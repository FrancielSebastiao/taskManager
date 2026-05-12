package com.fransebastiao.taskmanager.dto.response;

import java.time.LocalDate;

public record TimelinePointDto(
    LocalDate date,
    long      created,
    long      completed,
    long      inProgress,
    long      overdue
) {
    /**
     * Constructor for JPQL queries using FUNCTION('DATE', ...) or CAST(... AS date)
     * These return java.sql.Date, not java.time.LocalDate
     */
    public TimelinePointDto(
        java.sql.Date date,     // ✅ This is what JPQL actually returns
        Long created, 
        Long completed, 
        Long inProgress, 
        Long overdue
    ) {
        this(
            date != null ? date.toLocalDate() : null,
            created    != null ? created    : 0L,
            completed  != null ? completed  : 0L,
            inProgress != null ? inProgress : 0L,
            overdue    != null ? overdue    : 0L
        );
    }
}