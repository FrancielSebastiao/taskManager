package com.fransebastiao.taskmanager.dto.response;

import com.fransebastiao.taskmanager.domain.task.Task;

public record StatusDistributionDto(
    Task.TaskStatus status,
    Long            count
) {
    public StatusDistributionDto(Task.TaskStatus status, Long count) {
        this.status = status;
        this.count  = count;
    }
}