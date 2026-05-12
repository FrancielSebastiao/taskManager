package com.fransebastiao.taskmanager.dto.response;

public record TaskStatsResponse(
    long total,
    long completed,
    long inProgress,
    long overdue
) {}
