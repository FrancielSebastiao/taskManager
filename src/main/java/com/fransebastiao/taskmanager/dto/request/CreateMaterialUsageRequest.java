package com.fransebastiao.taskmanager.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateMaterialUsageRequest(
    @NotNull UUID projectId,
    @NotNull UUID materialId,
    @NotNull BigDecimal quantityUsed,
    @NotNull LocalDate usageDate,
    String notes,
    UUID recordedById
) {}
