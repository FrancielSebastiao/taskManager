package com.fransebastiao.taskmanager.dto.response;

import java.time.LocalDate;
import java.util.UUID;
import java.util.List;

import com.fransebastiao.taskmanager.domain.task.Task;

public record TaskFilter(
    List<Task.TaskStatus> statuses,
    List<Task.Priority>   priorities,
    String                category,
    UUID                  assigneeId,
    UUID                  projectId,
    LocalDate             dueDateFrom,
    LocalDate             dueDateTo,
    boolean               overdueOnly,
    Boolean               myTasksOnly,  // ← NOVO: null = all tasks, true = my tasks only
    int                   page,
    int                   size,
    String                sortBy,
    String                sortDir,
    String                search
) {
    public TaskFilter {
        page    = page    <= 0 ? 0  : page;
        size    = size    <= 0 ? 20 : Math.min(size, 100);
        sortBy  = sortBy  == null ? "createdAt" : sortBy;
        sortDir = sortDir == null ? "desc"      : sortDir;
    }
}

