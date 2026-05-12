package com.fransebastiao.taskmanager.dto.response;

import java.util.UUID;
import java.time.LocalDateTime;

import com.fransebastiao.taskmanager.domain.task.TaskComment;

public record TaskCommentResponse(
    UUID                          id,
    UUID                          taskId,
    String                        taskTitle,
    String                        authorName,
    TaskComment.CommentCategory   category,
    String                        categoryLabel,
    String                        content,
    LocalDateTime                 createdAt,
    LocalDateTime                 updatedAt
) {}
