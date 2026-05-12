package com.fransebastiao.taskmanager.service.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.dto.response.AnalyticsDashboardResponse;
import com.fransebastiao.taskmanager.dto.response.MetricsSummaryDto;
import com.fransebastiao.taskmanager.dto.response.TaskMetricsDto;
import com.fransebastiao.taskmanager.repository.AnalyticsRepository;
import com.fransebastiao.taskmanager.service.AnalyticsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsDashboardResponse getDashboard(LocalDate from, LocalDate to) {

        TaskMetricsDto current  = analyticsRepository.getTaskMetrics(from, to);

        long days = ChronoUnit.DAYS.between(from, to) + 1;

        LocalDate prevFrom = from.minusDays(days);
        LocalDate prevTo   = from.minusDays(1);

        TaskMetricsDto previous = analyticsRepository.getTaskMetrics(prevFrom, prevTo);

        return new AnalyticsDashboardResponse(
                buildMetrics(current, previous),
                analyticsRepository.getMonthlyTrend(7),
                analyticsRepository.getStatusDistribution(from, to),
                analyticsRepository.getTeamPerformance(from, to),
                analyticsRepository.getCategoryDistribution(from, to),
                analyticsRepository.getLaborSummary(from, to),
                analyticsRepository.getRecentActivities(20)
        );
    }

    private MetricsSummaryDto buildMetrics(TaskMetricsDto cur, TaskMetricsDto prev) {
        double curRate  = cur.totalTasks()  == 0 ? 0 : cur.completedTasks()  * 100.0 / cur.totalTasks();
        double prevRate = prev.totalTasks() == 0 ? 0 : prev.completedTasks() * 100.0 / prev.totalTasks();

        return new MetricsSummaryDto(
                cur.totalTasks(),
                delta(cur.totalTasks(), prev.totalTasks()),
                Math.round(curRate * 10.0) / 10.0,
                Math.round((curRate - prevRate) * 10.0) / 10.0,
                cur.activeUsers(),
                delta(cur.activeUsers(), prev.activeUsers()),
                cur.avgCompletionDays(),
                cur.avgCompletionDays() - prev.avgCompletionDays()
        );
    }

    private double delta(Long current, Long previous) {
        if (previous == null || previous == 0) return 0;
        return Math.round(((current - previous) * 100.0 / previous) * 10.0) / 10.0;
    }
}
