package com.fransebastiao.taskmanager.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.response.ProjectDashboardResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectStatsDto;
import com.fransebastiao.taskmanager.repository.LaborEntryRepository;
import com.fransebastiao.taskmanager.repository.MaterialUsageRepository;
import com.fransebastiao.taskmanager.repository.ProjectMemberRepository;
import com.fransebastiao.taskmanager.repository.ProjectRepository;
import com.fransebastiao.taskmanager.repository.TaskRepository;
import com.fransebastiao.taskmanager.service.ProjectDashboardStatsService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectDashboardStatsServiceImpl implements ProjectDashboardStatsService {
    private final ProjectRepository       projectRepository;
    private final TaskRepository          taskRepository;
    private final ProjectMemberRepository memberRepository;
    private final LaborEntryRepository    laborEntryRepository;
    private final MaterialUsageRepository materialUsageRepository;

    public ProjectDashboardResponse getStats(User currentUser, boolean isPrivileged) {
        boolean canSeeCosts = hasPrivilege(currentUser, "LER_CUSTOS");

        List<ProjectStatsDto> stats = new ArrayList<>();
        stats.add(buildProjectsStat(currentUser, isPrivileged));
        stats.add(buildTasksStat(currentUser, isPrivileged));
        stats.add(buildTeamStat(currentUser, isPrivileged));
        stats.add(canSeeCosts ? buildBudgetStat() : buildCompletionRateStat(currentUser));

        return new ProjectDashboardResponse(stats);
    }

    // -------------------------------------------------------------------------
    // Stats individuais
    // -------------------------------------------------------------------------

    private ProjectStatsDto buildProjectsStat(User userDetails, boolean isPrivileged) {
        long total;
        long active;
        long planning;
        long completed;
        long onHold;
    
        if (isPrivileged) {
            total      = projectRepository.count();
            active     = projectRepository.countByStatus(Project.ProjectStatus.EM_PROGRESSO);
            planning   = projectRepository.countByStatus(Project.ProjectStatus.PLANEANDO);
            completed  = projectRepository.countByStatus(Project.ProjectStatus.COMPLETO);
            onHold     = projectRepository.countByStatus(Project.ProjectStatus.EM_PAUSA);
        } else {
            total      = projectRepository.countByUser(userDetails.getId());
            active     = projectRepository.countByUserAndStatus(userDetails.getId(), Project.ProjectStatus.EM_PROGRESSO);
            planning   = projectRepository.countByUserAndStatus(userDetails.getId(), Project.ProjectStatus.PLANEANDO);
            completed  = projectRepository.countByUserAndStatus(userDetails.getId(), Project.ProjectStatus.COMPLETO);
            onHold     = projectRepository.countByUserAndStatus(userDetails.getId(), Project.ProjectStatus.EM_PAUSA);
        }

        return new ProjectStatsDto(
                "Total de Projectos",
                String.valueOf(total),
                "folder_special",
                "icon-bg--blue",
                "icon--blue",
                buildProjectNote(active, planning, completed, onHold),
                "note--muted",
                "info"
        );
    }

    private ProjectStatsDto buildTasksStat(User userDetails, boolean isPrivileged) {
        long total;
        long completed;
        long overdue;

        if (isPrivileged) {
            total     = taskRepository.count();
            completed = taskRepository.countByStatus(Task.TaskStatus.COMPLETA);
            overdue   = taskRepository.countOverdue(LocalDate.now());
        } else {
            total     = taskRepository.countByAssignee(userDetails.getId());
            completed = taskRepository.countByAssigneeAndStatus(userDetails.getId(), Task.TaskStatus.COMPLETA);
            overdue   = taskRepository.countOverdueByAssignee(userDetails.getId(), LocalDate.now(), Task.TaskStatus.COMPLETA);
        }

        String noteColor = overdue > 0 ? "note--warning" : "note--muted";
        String noteIcon = overdue > 0 ? "warning" : "check_circle";
        String note = completed + " concluídas" +
                (overdue > 0 ? ", " + overdue + " em atraso" : "");

        return new ProjectStatsDto(
                "Tarefas Totais",
                String.valueOf(total),
                "task_alt",
                "icon-bg--green",
                "icon--green",
                note,
                noteColor,
                noteIcon
        );
    }

    private ProjectStatsDto buildTeamStat(User userDetails, boolean isPrivileged) {
        long totalMembers;
        long activeMembers;

        if (isPrivileged) {
            totalMembers  = memberRepository.countDistinctUsers();
            activeMembers = memberRepository.countUsersInActiveProjects();
        } else {
            totalMembers  = memberRepository.countTeamMembers(userDetails.getId());
            activeMembers = memberRepository.countActiveTeamMembers(userDetails.getId());
        }

        return new ProjectStatsDto(
                "Membros da Equipa",
                String.valueOf(totalMembers),
                "group",
                "icon-bg--purple",
                "icon--purple",
                activeMembers + " ativos em projectos",
                "note--muted",
                "groups"
        );
    }

    private ProjectStatsDto buildBudgetStat() {
        BigDecimal totalBudget = projectRepository.sumBudget();
        if (totalBudget == null) totalBudget = BigDecimal.ZERO;

        BigDecimal laborSpent = laborEntryRepository.sumFinalAmounts();
        if (laborSpent == null) laborSpent = BigDecimal.ZERO;

        BigDecimal materialSpent = materialUsageRepository.sumTotalCosts();
        if (materialSpent == null) materialSpent = BigDecimal.ZERO;

        BigDecimal totalSpent = laborSpent.add(materialSpent);

        String noteColor = totalSpent.compareTo(totalBudget) > 0
            ? "note--danger"
            : "note--muted";

        String noteIcon = totalSpent.compareTo(totalBudget) > 0
            ? "error"
            : "payments";

        return new ProjectStatsDto(
                "Orçamento Total",
                formatCurrency(totalBudget),
                "payments",
                "icon-bg--amber",
                "icon--amber",
                formatCurrency(totalSpent) + " gastos",
                noteColor,
                noteIcon
        );
    }

    private ProjectStatsDto buildCompletionRateStat(User user) {
        long total     = taskRepository.countByAssignee(user.getId());
        long completed = taskRepository.countByAssigneeAndStatus(user.getId(), Task.TaskStatus.COMPLETA);
        
        double rate    = total == 0 ? 0 :
                Math.round((completed * 100.0 / total) * 10.0) / 10.0;

        String noteColor = rate >= 80 ? "note--success" :
                           rate >= 50 ? "note--warning" : "note--danger";

        String noteIcon = rate >= 80 ? "trending_up" :
                          rate >= 50 ? "trending_flat" :
                                       "trending_down";
        
        return new ProjectStatsDto(
                "Taxa de Conclusão",
                rate + "%",
                "trending_up",
                "icon-bg--teal",
                "icon--teal",
                completed + " de " + total + " tarefas concluídas",
                noteColor, 
                noteIcon
        );
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private String buildProjectNote(long active, long planning,
                                     long completed, long onHold) {
        List<String> parts = new ArrayList<>();
        if (active    > 0) parts.add(active    + " ativos");
        if (planning  > 0) parts.add(planning  + " em planeamento");
        if (completed > 0) parts.add(completed + " concluído" + (completed > 1 ? "s" : ""));
        if (onHold    > 0) parts.add(onHold    + " em pausa");
        return String.join(", ", parts);
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0 Kz";
        if (value.compareTo(BigDecimal.valueOf(1_000_000)) >= 0) {
            return value.divide(BigDecimal.valueOf(1_000_000), 1, RoundingMode.HALF_UP) + "M" + " Kz";
        }
        if (value.compareTo(BigDecimal.valueOf(1_000)) >= 0) {
            return value.divide(BigDecimal.valueOf(1_000), 1, RoundingMode.HALF_UP) + "k" + " Kz";
        }
        return value.setScale(0, RoundingMode.HALF_UP) + " Kz";
    }

    private boolean hasPrivilege(User user, String privilege) {
        return user.getRoles().stream()
                .flatMap(r -> r.getPrivileges().stream())
                .anyMatch(p -> p.getName().equals(privilege));
    }
}
