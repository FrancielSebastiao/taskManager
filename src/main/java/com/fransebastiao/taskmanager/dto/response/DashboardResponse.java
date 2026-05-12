package com.fransebastiao.taskmanager.dto.response;

import java.util.List;

public record DashboardResponse(
    List<StatCardDto> stats,
    List<TimelinePointDto> timeline,
    PagedResponse<DashboardRecentActivityDto> recentActivities,
    PagedResponse<UpcomingTaskDto> upcomingTasks
) {}
