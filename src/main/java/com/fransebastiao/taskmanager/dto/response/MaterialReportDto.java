package com.fransebastiao.taskmanager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaterialReportDto(
    String     materialName,
    String     unit,
    BigDecimal quantityUsed,
    BigDecimal unitPrice,
    BigDecimal totalCost,
    LocalDate  usageDate,
    String     projectName
) {}
