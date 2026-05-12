package com.fransebastiao.taskmanager.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateLaborEntryRequest(
    @NotNull UUID taskId,
    @NotNull UUID workerId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate expectedEndDate,
    @NotNull BigDecimal agreedAmount
) {}

