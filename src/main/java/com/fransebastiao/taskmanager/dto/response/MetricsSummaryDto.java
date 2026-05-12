package com.fransebastiao.taskmanager.dto.response;

public record MetricsSummaryDto(
    Long   totalTasks,
    Double totalTasksDelta,       // % vs mês anterior
    Double completionRate,
    Double completionRateDelta,
    Long   activeUsers,
    Double activeUsersDelta,
    Double avgCompletionDays,
    Double avgCompletionDaysDelta // negativo = melhorou
) {}
