package com.fransebastiao.taskmanager.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskReportDto(
    String          title,
    String          categoryName,
    String          status,
    String          priority,
    LocalDate       dueDate,
    LocalDateTime   completedAt,
    Integer         progressPercent,
    boolean         overdue
) {}
