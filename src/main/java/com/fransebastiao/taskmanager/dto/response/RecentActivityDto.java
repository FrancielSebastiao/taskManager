package com.fransebastiao.taskmanager.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fransebastiao.taskmanager.domain.task.Task;

public record RecentActivityDto(
    UUID            id,
    String          title,
    String          categoryName,
    Task.TaskStatus status,
    LocalDateTime   createdAt,
    LocalDateTime   completedAt
) {
    public RecentActivityDto(UUID id, String title, String categoryName, Task.TaskStatus status, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.title = title;
        this.categoryName = categoryName;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }
}