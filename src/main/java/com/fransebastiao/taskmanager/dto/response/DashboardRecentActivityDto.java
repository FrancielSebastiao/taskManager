package com.fransebastiao.taskmanager.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

import com.fransebastiao.taskmanager.domain.task.Task;

public record DashboardRecentActivityDto(
    UUID                    id,
    String                  title,
    String                  categoryName,
    Task.TaskStatus         status,
    LocalDateTime           createdAt,
    LocalDateTime           completedAt,
    List<AssigneeAvatarDto> assignees 
) {}