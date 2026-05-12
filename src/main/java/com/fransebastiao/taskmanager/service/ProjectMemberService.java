package com.fransebastiao.taskmanager.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.domain.project.ProjectMember;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.request.CreateCategoryRequest;
import com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse;
import com.fransebastiao.taskmanager.dto.response.NameResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectMemberResponse;

public interface ProjectMemberService {
    ProjectMemberResponse adicionarMembro(UUID projectId, UUID userId, String role);
    void removerMembro(UUID projectId, UUID userId);
    ProjectMemberResponse actualizarRole(UUID projectId, UUID userId, String novaRole);
    Page<ProjectMemberResponse> listarMembrosPorProjecto(UUID projectId, Pageable pageable);
    Page<ProjectMemberResponse> listarProjectosPorUser(UUID userId, Pageable pageable);
    ProjectMember ensureMember(Project project, User user, String roleName);

    // Member role
    NameAndDescriptionResponse createMemberRole(CreateCategoryRequest request);
    NameAndDescriptionResponse updateMemberRole(UUID id, CreateCategoryRequest request);
    Page<NameResponse> getMemberRoles(Pageable pageable);
    void deleteMemberRole(UUID id);
}
