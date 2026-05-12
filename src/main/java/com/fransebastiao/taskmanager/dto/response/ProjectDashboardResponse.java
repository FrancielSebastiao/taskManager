package com.fransebastiao.taskmanager.dto.response;

import java.util.List;

public record ProjectDashboardResponse(
    List<ProjectStatsDto> stats
) {}
