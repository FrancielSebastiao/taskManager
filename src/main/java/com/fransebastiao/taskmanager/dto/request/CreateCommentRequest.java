package com.fransebastiao.taskmanager.dto.request;

import com.fransebastiao.taskmanager.domain.task.TaskComment;

import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(
    @NotNull TaskComment.CommentCategory category,
    String content
) {}
