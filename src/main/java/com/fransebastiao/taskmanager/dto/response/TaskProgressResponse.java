package com.fransebastiao.taskmanager.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskProgressResponse(
    UUID          taskId,
    String        taskTitle,
    Integer       progressPercent,
    String        status,
    LocalDateTime lastUpdatedAt
) {}
