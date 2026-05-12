package com.fransebastiao.taskmanager.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.fransebastiao.taskmanager.domain.project.Project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProjectRequest(
    @NotBlank String name,
    String description,
    Project.Priority priority,
    Project.ProjectStatus status,
    @NotNull LocalDate startDate,
    @NotNull LocalDate deadline,
    @NotNull UUID managerId,
    @NotNull UUID categoryId,
    BigDecimal budget
) {}
