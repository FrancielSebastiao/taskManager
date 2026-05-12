package com.fransebastiao.taskmanager.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fransebastiao.taskmanager.domain.task.Task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTaskRequest(
    @NotBlank String title,
    String description,
    @NotNull LocalDate dueDate,
    UUID projectId,
    List<UUID> assigneeIds,
    Task.Priority priority,
    Task.TaskStatus status,
    UUID categoryId,
    BigDecimal estimatedHours
) {}