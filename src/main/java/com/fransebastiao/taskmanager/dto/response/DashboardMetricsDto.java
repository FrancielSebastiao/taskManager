package com.fransebastiao.taskmanager.dto.response;

public record DashboardMetricsDto(
    long total,
    long createdInPeriod,        // Tasks created during [from, to]
    long completed,
    long completedInPeriod,
    long inProgress,
    long overdue,
    long teamSize
) {}