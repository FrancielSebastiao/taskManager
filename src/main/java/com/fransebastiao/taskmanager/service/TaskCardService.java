package com.fransebastiao.taskmanager.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import com.fransebastiao.taskmanager.dto.response.TaskFilter;
import com.fransebastiao.taskmanager.dto.response.TaskStatsResponse;
import com.fransebastiao.taskmanager.dto.response.TaskSummaryResponse;

public interface TaskCardService {
    Page<TaskSummaryResponse> findTasks(TaskFilter filter, Authentication authentication);
    TaskStatsResponse getMyTaskStats(Authentication authentication);
    boolean canAccessTask(UUID taskId, Authentication authentication);

}
