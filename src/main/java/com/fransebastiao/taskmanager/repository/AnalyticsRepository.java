package com.fransebastiao.taskmanager.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fransebastiao.taskmanager.dto.response.CategoryDistributionDto;
import com.fransebastiao.taskmanager.dto.response.DashboardMetricsDto;
import com.fransebastiao.taskmanager.dto.response.LaborSummaryDto;
import com.fransebastiao.taskmanager.dto.response.MonthlyTrendDto;
import com.fransebastiao.taskmanager.dto.response.RecentActivityDto;
import com.fransebastiao.taskmanager.dto.response.StatusDistributionDto;
import com.fransebastiao.taskmanager.dto.response.TaskMetricsDto;
import com.fransebastiao.taskmanager.dto.response.TeamPerformanceDto;
import com.fransebastiao.taskmanager.dto.response.TimelinePointDto;

public interface AnalyticsRepository {

    // Métricas principais com comparação vs mês anterior
    TaskMetricsDto getTaskMetrics(LocalDate from, LocalDate to);

    // Tendência mensal (criadas vs concluídas)
    List<MonthlyTrendDto> getMonthlyTrend(int months);

    // Distribuição por status
    List<StatusDistributionDto> getStatusDistribution(LocalDate from, LocalDate to);

    // Desempenho por membro
    List<TeamPerformanceDto> getTeamPerformance(LocalDate from, LocalDate to);

    // Distribuição por categoria
    List<CategoryDistributionDto> getCategoryDistribution(LocalDate from, LocalDate to);

    // Registo de labour entries
    List<LaborSummaryDto> getLaborSummary(LocalDate from, LocalDate to);

    // Atividades recentes
    List<RecentActivityDto> getRecentActivities(int limit);

    DashboardMetricsDto buildMetrics(
        LocalDate from,
        LocalDate to,
        UUID userId,
        boolean isPrivileged
    );

    List<TimelinePointDto> getTimeline(LocalDate from, LocalDate to, UUID userId, boolean privileged);
}
