package com.fransebastiao.taskmanager.service;

import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.response.ProjectDashboardResponse;

public interface ProjectDashboardStatsService {
    ProjectDashboardResponse getStats(User currentUser, boolean isPrivileged);
}
