package com.fransebastiao.taskmanager.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.dto.request.CreateProjectRequest;
import com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectResponse;

public interface ProjectService {
    ProjectResponse create(CreateProjectRequest request);
    ProjectResponse update(UUID id, CreateProjectRequest request);
    ProjectResponse getById(UUID id);
    List<NameAndDescriptionResponse> getProjectNameAndDescription();
    ProjectResponse updateStatus(UUID id, Project.ProjectStatus novoStatus, UUID userId);
    ProjectResponse updateOrcamento(UUID id, BigDecimal novoOrcamento);
    void delete(UUID id);
}
