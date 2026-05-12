package com.fransebastiao.taskmanager.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fransebastiao.taskmanager.domain.task.Task;

public record UpcomingTaskDto(
    UUID                    id,
    String                  title,
    LocalDate               dueDate,
    Task.TaskStatus         status,
    Task.Priority           priority,
    int                     progressPercent,
    List<AssigneeAvatarDto> assignees
) {}