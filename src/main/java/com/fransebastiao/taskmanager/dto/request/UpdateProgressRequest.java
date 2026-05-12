package com.fransebastiao.taskmanager.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateProgressRequest(
    @NotNull @Min(0) @Max(100) Integer progressPercent
) {}
