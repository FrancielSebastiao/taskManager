package com.fransebastiao.taskmanager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TaskDetailResponse(
    UUID id,
    String title,
    String description,
    String status,
    String priority,
    String categoryName,
    Integer progressPercent,
    LocalDate dueDate,
    String dueDateRelative,
    LocalDate createdAt,
    BigDecimal estimatedHours,
    String projectName,
    AssigneeAvatarDto createdBy,
    PagedResponse<AssigneeDetailDto> assignees,
    PagedResponse<TaskImageDto> images,
    PagedResponse<TaskFileDto> attachments,
    PagedResponse<TaskCommentDetailDto> comments,
    PagedResponse<TaskActivityDto> activityLog
) {}
