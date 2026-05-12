package com.fransebastiao.taskmanager.dto.response;

public record TaskBreakdownDto(
    long total,
    long completed,
    long inProgress,
    long pending
) {}
