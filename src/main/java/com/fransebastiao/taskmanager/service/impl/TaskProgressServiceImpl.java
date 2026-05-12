package com.fransebastiao.taskmanager.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.domain.task.TaskActivity;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.request.UpdateProgressRequest;
import com.fransebastiao.taskmanager.dto.response.TaskProgressResponse;
import com.fransebastiao.taskmanager.exception.custom.ResourceNotFoundException;
import com.fransebastiao.taskmanager.exception.custom.UnauthorizedException;
import com.fransebastiao.taskmanager.repository.ProjectMemberRepository;
import com.fransebastiao.taskmanager.repository.TaskActivityRepository;
import com.fransebastiao.taskmanager.repository.TaskRepository;
import com.fransebastiao.taskmanager.repository.UserRepository;
import com.fransebastiao.taskmanager.service.TaskProgressService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TaskProgressServiceImpl implements TaskProgressService {

    private final TaskRepository          taskRepository;
    private final UserRepository          userRepository;
    private final ProjectMemberRepository memberRepository;
    private final TaskActivityRepository  taskActivityRepository;

    public TaskProgressResponse actualizarProgresso(UUID taskId, UUID userId,
                                                     UpdateProgressRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (task.getProject() != null) {
            validarMembroDoProjecto(task.getProject().getId(), userId);
        }

        task.actualizarProgresso(request.progressPercent(), user); // lógica na entidade

        log.info("Task {} progress updated to {}% by user {}",
                taskId, request.progressPercent(), userId);

        taskActivityRepository.save(new TaskActivity(
            task, user,
            String.format("Progresso actualizado para %d%%", request.progressPercent()),
            TaskActivity.ActivityType.PROGRESS_UPDATED)
        );

        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public TaskProgressResponse buscarProgresso(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskProgressResponse> listarProgressoPorProjecto(UUID projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private void validarMembroDoProjecto(UUID projectId, UUID userId) {
        if (!memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new UnauthorizedException("User is not a member of this project");
        }
    }

    public TaskProgressResponse toResponse(Task t) {
        return new TaskProgressResponse(
                t.getId(),
                t.getTitle(),
                t.getProgressPercent(),
                t.getStatus().name(),
                t.getLastProgressUpdatedAt()
        );
    }
}
