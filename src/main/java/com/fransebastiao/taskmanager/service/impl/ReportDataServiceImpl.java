package com.fransebastiao.taskmanager.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.resource.MaterialUsage;
import com.fransebastiao.taskmanager.domain.task.LaborEntry;
import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.response.LaborReportDto;
import com.fransebastiao.taskmanager.dto.response.MaterialReportDto;
import com.fransebastiao.taskmanager.dto.response.ReportData;
import com.fransebastiao.taskmanager.dto.response.TaskReportDto;
import com.fransebastiao.taskmanager.exception.custom.UnauthorizedException;
import com.fransebastiao.taskmanager.repository.LaborEntryRepository;
import com.fransebastiao.taskmanager.repository.MaterialUsageRepository;
import com.fransebastiao.taskmanager.repository.ProjectMemberRepository;
import com.fransebastiao.taskmanager.repository.TaskRepository;
import com.fransebastiao.taskmanager.repository.UserRepository;
import com.fransebastiao.taskmanager.service.ReportDataService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportDataServiceImpl implements ReportDataService {

    private final UserRepository          userRepository;
    private final TaskRepository          taskRepository;
    private final LaborEntryRepository    laborEntryRepository;
    private final MaterialUsageRepository materialUsageRepository;
    private final ProjectMemberRepository memberRepository;

    /**
     * Verifica se o requesterId tem permissão para gerar relatório do targetUserId.
     * Admin → qualquer utilizador
     * Manager/Supervisor/Engineer → apenas membros dos seus projectos
     * Worker → apenas o próprio
     */
    public ReportData buildReportData(UUID requesterId, UUID targetUserId,
                                       LocalDate from, LocalDate to) {
        User requester = findUser(requesterId);
        User target    = findUser(targetUserId);

        validatePermission(requester, target);

        List<Task>          tasks   = taskRepository.findByAssigneeId(targetUserId)
                .stream()
                .filter(t -> !t.getCreatedAt().toLocalDate().isBefore(from)
                          && !t.getCreatedAt().toLocalDate().isAfter(to))
                .toList();

        List<LaborEntry>    labor   = laborEntryRepository.findByWorkerId(targetUserId)
                .stream()
                .filter(l -> !l.getStartDate().isBefore(from)
                          && !l.getStartDate().isAfter(to))
                .toList();

        List<MaterialUsage> mats    = materialUsageRepository
                .findByRecordedByIdAndDateRange(targetUserId, from, to);

        return buildData(target, from, to, tasks, labor, mats);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private void validatePermission(User requester, User target) {
        boolean isAdmin = hasRole(requester, "ROLE_ADMIN");
        if (isAdmin) return;

        boolean isSelf = requester.getId().equals(target.getId());
        if (isSelf) return;

        boolean isWorker = hasRole(requester, "ROLE_WORKER");
        if (isWorker) {
            throw new UnauthorizedException("Workers can only generate their own reports");
        }

        // Manager/Supervisor/Engineer — verificar se partilham um projecto
        boolean shareProject = memberRepository.findByUserId(requester.getId())
                .stream()
                .anyMatch(m -> memberRepository.existsByProjectIdAndUserId(
                        m.getProject().getId(), target.getId()));

        if (!shareProject) {
            throw new UnauthorizedException(
                    "You can only generate reports for members of your projects");
        }
    }

    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(roleName));
    }

    private ReportData buildData(User target, LocalDate from, LocalDate to,
                                  List<Task> tasks, List<LaborEntry> labor,
                                  List<MaterialUsage> mats) {
        long completed   = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETA).count();
        long inProgress  = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.EM_PROGRESSO).count();
        long pending     = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.PENDENTE).count();
        double rate      = tasks.isEmpty() ? 0 : Math.round(completed * 100.0 / tasks.size() * 10) / 10.0;

        double avgDays = labor.stream()
                .filter(LaborEntry::isCompleted)
                .mapToLong(l -> ChronoUnit.DAYS.between(l.getStartDate(), l.getActualEndDate()))
                .average().orElse(0);

        List<TaskReportDto> taskDtos = tasks.stream().map(t -> new TaskReportDto(
                t.getTitle(),
                t.getCategory() != null ? t.getCategory().getName() : "—",
                t.getStatus().name(),
                t.getPriority().name(),
                t.getDueDate(),
                t.getCompletedAt(),
                t.getProgressPercent(),
                t.isOverdue()
        )).toList();

        List<LaborReportDto> laborDtos = labor.stream().map(l -> {
            boolean done      = l.isCompleted();
            BigDecimal final_ = done ? l.calculateFinalAmount() : null;
            BigDecimal adj    = done ? final_.subtract(l.getAgreedAmount()) : null;
            return new LaborReportDto(
                    l.getTask().getTitle(),
                    l.getStartDate(), l.getExpectedEndDate(), l.getActualEndDate(),
                    l.getAllocatedDays(),
                    done ? l.getActualDays() : null,
                    l.getAgreedAmount(), final_, adj,
                    done ? (l.isEarly() ? "BONUS" : l.isLate() ? "DISCOUNT" : "ON_TIME") : "PENDING"
            );
        }).toList();

        List<MaterialReportDto> matDtos = mats.stream().map(m -> new MaterialReportDto(
                m.getMaterial().getName(),
                m.getMaterial().getUnit(),
                m.getQuantityUsed(),
                m.getMaterial().getUnitPrice(),
                m.getTotalCost(),
                m.getUsageDate(),
                m.getProject().getName()
        )).toList();

        BigDecimal totalAgreed   = labor.stream().map(LaborEntry::getAgreedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFinal    = labor.stream().filter(LaborEntry::isCompleted).map(LaborEntry::calculateFinalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalBonus    = labor.stream().filter(LaborEntry::isCompleted).map(LaborEntry::getBonus).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDiscount = labor.stream().filter(LaborEntry::isCompleted).map(LaborEntry::getDiscount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalMats     = mats.stream().map(MaterialUsage::getTotalCost).reduce(BigDecimal.ZERO, BigDecimal::add);

        String roleName = target.getRoles().stream().findFirst()
                .map(r -> r.getName().replace("ROLE_", "")).orElse("—");

        return ReportData.builder()
                .workerName(target.getName())
                .workerEmail(target.getEmail())
                .roleName(roleName)
                .periodFrom(from).periodTo(to)
                .totalTasks(tasks.size())
                .completedTasks(completed)
                .inProgressTasks(inProgress)
                .pendingTasks(pending)
                .completionRate(rate)
                .avgCompletionDays(Math.round(avgDays * 10) / 10.0)
                .tasks(taskDtos)
                .laborEntries(laborDtos)
                .totalAgreed(totalAgreed).totalFinal(totalFinal)
                .totalBonus(totalBonus).totalDiscount(totalDiscount)
                .materials(matDtos)
                .totalMaterialCost(totalMats)
                .totalProjectCost(totalFinal.add(totalMats))
                .build();
    }

    private User findUser(UUID id) {
        return userRepository.findByIdWithRoles(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }
}