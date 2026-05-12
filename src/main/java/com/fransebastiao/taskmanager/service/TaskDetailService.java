package com.fransebastiao.taskmanager.service;

import java.util.UUID;

import com.fransebastiao.taskmanager.dto.response.AssigneeDetailDto;
import com.fransebastiao.taskmanager.dto.response.PagedResponse;
import com.fransebastiao.taskmanager.dto.response.TaskActivityDto;
import com.fransebastiao.taskmanager.dto.response.TaskCommentDetailDto;
import com.fransebastiao.taskmanager.dto.response.TaskDetailResponse;
import com.fransebastiao.taskmanager.dto.response.TaskFileDto;
import com.fransebastiao.taskmanager.dto.response.TaskImageDto;

public interface TaskDetailService {
    TaskDetailResponse getDetail(UUID taskId);
    PagedResponse<AssigneeDetailDto> getTaskAssigness(UUID taskId, int page);
    PagedResponse<TaskImageDto> getTaskImages(UUID taskId, int page);
    PagedResponse<TaskFileDto> getTaskFiles(UUID taskId, int page);
    PagedResponse<TaskCommentDetailDto> getTaskComments(UUID taskId, int page);
    PagedResponse<TaskActivityDto> getTaskActivities(UUID taskId, int page);
}
