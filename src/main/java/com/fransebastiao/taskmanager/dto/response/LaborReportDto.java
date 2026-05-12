package com.fransebastiao.taskmanager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LaborReportDto(
    String     taskTitle,
    LocalDate  startDate,
    LocalDate  expectedEndDate,
    LocalDate  actualEndDate,
    long       allocatedDays,
    Long       actualDays,
    BigDecimal agreedAmount,
    BigDecimal finalAmount,
    BigDecimal adjustment,
    String     type  // BONUS, DISCOUNT, ON_TIME, PENDING
) {}
