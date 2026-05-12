package com.fransebastiao.taskmanager.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fransebastiao.taskmanager.dto.response.DashboardRecentActivityDto;
import com.fransebastiao.taskmanager.dto.response.DashboardResponse;
import com.fransebastiao.taskmanager.dto.response.PagedResponse;
import com.fransebastiao.taskmanager.dto.response.TimelinePointDto;
import com.fransebastiao.taskmanager.dto.response.UpcomingTaskDto;

public interface DashboardService {
    DashboardResponse getDashboard(
            LocalDate from,
            LocalDate to,
            UUID userId,
            boolean isPrivileged
    ) ;
    List<TimelinePointDto> getTimeline(LocalDate from, LocalDate to, UUID userId, boolean privileged);
    PagedResponse<UpcomingTaskDto> getUpcomingTasks(UUID userId, boolean privileged, int pageIndex);
    PagedResponse<DashboardRecentActivityDto> getRecentActivities(UUID userId, boolean privileged, int pageIndex);
}
