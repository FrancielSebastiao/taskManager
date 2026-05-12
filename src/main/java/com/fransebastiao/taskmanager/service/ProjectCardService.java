package com.fransebastiao.taskmanager.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.dto.response.ProjectFilter;
import com.fransebastiao.taskmanager.dto.response.ProjectStatsResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectSummaryResponse;

public interface ProjectCardService {
    Page<ProjectSummaryResponse> findProjects(ProjectFilter filter, Authentication authentication);
    boolean canAccessProject(UUID projectId, Authentication authentication);
    ProjectStatsResponse getMyProjectStats(Authentication authentication);
    ProjectSummaryResponse buscarCard(UUID projectId);
    ProjectSummaryResponse toCardResponse(Project p);
}
