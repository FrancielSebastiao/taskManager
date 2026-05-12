package com.fransebastiao.taskmanager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.domain.task.Task;

public record ProjectResponse(
    UUID id,
    String name,
    String description,
    LocalDate startDate,
    LocalDate deadline,
    String status,
    BigDecimal budget,

    // Manager
    UUID managerId,
    String managerName,
    String managerEmail,

    // Category
    UUID categoryId,
    String categoryName,

    // Costs
    BigDecimal totalLaborCost,
    BigDecimal totalMaterialCost,
    BigDecimal totalCost,

    // Progress
    int progress,
    int completinRate,

    // Summary counts
    int totalMembers,
    int totalTasks,
    int completedTasks,

    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ProjectResponse from(Project p) {
        return new ProjectResponse(
            p.getId(),
            p.getName(),
            p.getDescription(),
            p.getStartDate(),
            p.getDeadline(),
            p.getStatus().name(),

            p.getBudget(),

            p.getManager().getId(),
            p.getManager().getName(),
            p.getManager().getEmail(),
            
            p.getCategory() != null ? p.getCategory().getId() : null,
            p.getCategory() != null ? p.getCategory().getName() : null,

            p.getTotalLaborCost(),
            p.getTotalMaterialCost(),
            p.getTotalCost(),

            p.calculateProgress(),
            p.getCompletionRateProgress(),

            p.getTeam().size(),
            p.getTasks().size(),
            (int) p.getTasks().stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETA).count(),

            p.getCreatedAt(),
            p.getUpdatedAt()
        );
    }
}
