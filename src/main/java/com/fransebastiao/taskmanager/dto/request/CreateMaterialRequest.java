package com.fransebastiao.taskmanager.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMaterialRequest(
    @NotBlank String name,
    @NotBlank String unit,
    @NotNull BigDecimal unitPrice
) {}
