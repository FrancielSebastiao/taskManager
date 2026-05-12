package com.fransebastiao.taskmanager.dto.response;

public record TaskMetricsDto(
    Long   totalTasks,
    Long   completedTasks,
    Long   activeUsers,
    Double avgCompletionDays
) {}
