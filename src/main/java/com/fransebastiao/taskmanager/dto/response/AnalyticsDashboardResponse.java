package com.fransebastiao.taskmanager.dto.response;

import java.util.List;

public record AnalyticsDashboardResponse(
    MetricsSummaryDto              metrics,
    List<MonthlyTrendDto>          monthlyTrend,
    List<StatusDistributionDto>    statusDistribution,
    List<TeamPerformanceDto>       teamPerformance,
    List<CategoryDistributionDto>  categoryDistribution,
    List<LaborSummaryDto>          laborSummary,
    List<RecentActivityDto>        recentActivities
) {}
