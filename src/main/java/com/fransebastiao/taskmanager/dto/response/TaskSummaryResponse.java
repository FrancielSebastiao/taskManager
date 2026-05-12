package com.fransebastiao.taskmanager.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TaskSummaryResponse(
    UUID                    id,
    String                  title,
    String                  description,
    String                  status,
    String                  priority,
    Integer                 progressPercent,
    LocalDate               dueDate,
    boolean                 overdue,
    List<AssigneeAvatarDto> assignees
) {}