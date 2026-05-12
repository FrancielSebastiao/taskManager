package com.fransebastiao.taskmanager.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.task.LaborEntry;
import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.request.CreateLaborEntryRequest;
import com.fransebastiao.taskmanager.dto.response.LaborEntryResponse;
import com.fransebastiao.taskmanager.repository.LaborEntryRepository;
import com.fransebastiao.taskmanager.repository.TaskRepository;
import com.fransebastiao.taskmanager.repository.UserRepository;
import com.fransebastiao.taskmanager.service.LaborEntryService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LaborEntryServiceImpl implements LaborEntryService {

    private final LaborEntryRepository laborEntryRepository;
    private final TaskRepository       taskRepository;
    private final UserRepository       userRepository;

    public LaborEntry criar(CreateLaborEntryRequest request) {
        Task task   = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        User worker = userRepository.findById(request.workerId())
                .orElseThrow(() -> new EntityNotFoundException("Worker not found"));

        LaborEntry entry = new LaborEntry(
                task, worker,
                request.startDate(),
                request.expectedEndDate(),
                request.agreedAmount()
        );

        log.info("Labor entry created for worker {} on task {}", request.workerId(), request.taskId());
        return laborEntryRepository.save(entry);
    }

    public LaborEntry concluir(UUID id, LocalDate actualEndDate) {
        LaborEntry entry = buscarPorId(id);
        entry.complete(actualEndDate); // lógica na entidade

        log.info("Labor entry {} completed — final amount: {}",
                id, entry.calculateFinalAmount());
        return entry;
    }

    @Transactional(readOnly = true)
    public LaborEntry buscarPorId(UUID id) {
        return laborEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Labor entry not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<LaborEntryResponse> listarPorProjecto(UUID projectId) {
        return laborEntryRepository.findByProjectId(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LaborEntryResponse> listarPendentesPorWorker(UUID workerId) {
        return laborEntryRepository.findPendingByWorkerId(workerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularCustoTotalProjecto(UUID projectId) {
        return laborEntryRepository.findCompletedByProjectId(projectId).stream()
                .map(LaborEntry::calculateFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public LaborEntryResponse toResponse(LaborEntry e) {
        boolean completed = e.isCompleted();
        return new LaborEntryResponse(
                e.getId(),
                e.getWorker().getName(),
                e.getTask().getTitle(),
                e.getStartDate(),
                e.getExpectedEndDate(),
                e.getActualEndDate(),
                e.getAgreedAmount(),
                completed ? e.calculateFinalAmount() : null,
                completed ? e.getBonus()             : null,
                completed ? e.getDiscount()           : null,
                completed,
                completed && e.isLate(),
                completed && e.isEarly()
        );
    }
}
