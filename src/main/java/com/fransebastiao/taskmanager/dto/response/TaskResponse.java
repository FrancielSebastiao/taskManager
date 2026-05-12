package com.fransebastiao.taskmanager.dto.response;

import java.util.List;
import java.util.UUID;

import com.fransebastiao.taskmanager.domain.task.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
    UUID id,
        String title,
        String description,
        Task.TaskStatus status,
        Task.Priority priority,
        LocalDate dueDate,
        LocalDateTime completedAt,
        boolean isOverdue,

        // Project reference
        UUID projectId,
        String projectName,

        // Category
        UUID categoryId,
        String categoryName,

        // Progress
        int progressPercent,
        String lastProgressUpdatedById,
        LocalDateTime lastProgressUpdatedAt,

        // Assignees
        List<AssigneeDTO> assignees,

        // Counts
        int totalLaborEntries,
        int totalPhotos,
        int totalComments,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getCompletedAt(),
                task.isOverdue(),

                task.getProject() != null ? task.getProject().getId() : null,
                task.getProject() != null ? task.getProject().getName() : null,

                task.getCategory() != null ? task.getCategory().getId() : null,
                task.getCategory() != null ? task.getCategory().getName() : null,

                task.getProgressPercent(),
                task.getLastProgressUpdatedById(),
                task.getLastProgressUpdatedAt(),

                task.getAssignees().stream()
                        .map(AssigneeDTO::from)
                        .toList(),

                task.getLaborEntries().size(),
                task.getPhotos().size(),
                task.getComments().size(),

                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}