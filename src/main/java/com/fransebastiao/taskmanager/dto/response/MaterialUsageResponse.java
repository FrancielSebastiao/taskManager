package com.fransebastiao.taskmanager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MaterialUsageResponse(
    UUID       id,
    String     materialName,
    String     unit,
    BigDecimal quantityUsed,
    BigDecimal unitPrice,
    BigDecimal totalCost,
    LocalDate  usageDate,
    String     notes
) {}
