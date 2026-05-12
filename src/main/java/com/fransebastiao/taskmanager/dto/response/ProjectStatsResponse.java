package com.fransebastiao.taskmanager.dto.response;

public record ProjectStatsResponse(
    long total,
    long active,
    long planning,
    long completed,
    long asManager
) {}