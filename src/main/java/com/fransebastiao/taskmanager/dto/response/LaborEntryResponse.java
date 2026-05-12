package com.fransebastiao.taskmanager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LaborEntryResponse(
    UUID       id,
    String     workerName,
    String     taskTitle,
    LocalDate  startDate,
    LocalDate  expectedEndDate,
    LocalDate  actualEndDate,
    BigDecimal agreedAmount,
    BigDecimal finalAmount,
    BigDecimal bonus,
    BigDecimal discount,
    boolean    completed,
    boolean    late,
    boolean    early
) {}