package com.fransebastiao.taskmanager.service;

import java.util.UUID;

import com.fransebastiao.taskmanager.dto.response.ActivityDto;
import com.fransebastiao.taskmanager.dto.response.PagedResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectDetailResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectFileDto;
import com.fransebastiao.taskmanager.dto.response.TaskSummaryDto;
import com.fransebastiao.taskmanager.dto.response.TeamMemberDetailDto;

public interface ProjectDetailService {
    ProjectDetailResponse getDetail(UUID projectId);
    PagedResponse<TaskSummaryDto> getTasks(UUID projectId, int page);
    PagedResponse<ActivityDto> getActivities(UUID projectId, int page);
    PagedResponse<TeamMemberDetailDto> getTeam(UUID projectId, int page);
    PagedResponse<ProjectFileDto> getFiles(UUID projectId, int page);
}
