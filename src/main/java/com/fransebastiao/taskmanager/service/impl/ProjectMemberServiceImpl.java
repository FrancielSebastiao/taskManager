package com.fransebastiao.taskmanager.service.impl;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.domain.project.ProjectMember;
import com.fransebastiao.taskmanager.domain.project.ProjectMemberRole;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.request.CreateCategoryRequest;
import com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse;
import com.fransebastiao.taskmanager.dto.response.NameResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectMemberResponse;
import com.fransebastiao.taskmanager.exception.custom.DuplicateException;
import com.fransebastiao.taskmanager.exception.custom.ResourceNotFoundException;
import com.fransebastiao.taskmanager.mapper.NameAndDescriptionMapper;
import com.fransebastiao.taskmanager.repository.ProjectMemberRepository;
import com.fransebastiao.taskmanager.repository.ProjectMemberRoleRepository;
import com.fransebastiao.taskmanager.repository.ProjectRepository;
import com.fransebastiao.taskmanager.repository.UserRepository;
import com.fransebastiao.taskmanager.service.ProjectMemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository memberRepository;
    private final ProjectMemberRoleRepository roleRepository;
    private final ProjectRepository       projectRepository;
    private final UserRepository          userRepository;

    @Transactional
    public ProjectMemberResponse adicionarMembro(UUID projectId, UUID userId, String role) {
        if (memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new IllegalArgumentException("Úsuario já é um membro deste projecto");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projecto não foi encontrado"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Úsuario não foi encontrado"));
        ProjectMemberRole memberRole = null;
        if (role != null) {
            memberRole = roleRepository.findByName(role).orElseThrow(() -> new ResourceNotFoundException("Função de membro não foi encontrada"));
        }

        ProjectMember member = new ProjectMember(project, user, memberRole);
        log.info("User {} added to project {} as {}", userId, projectId, role);
        return ProjectMemberResponse.from(memberRepository.save(member));
    }

    @Transactional
    public void removerMembro(UUID projectId, UUID userId) {
        if (!memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ResourceNotFoundException("Member not found in project");
        }
        memberRepository.deleteByProjectIdAndUserId(projectId, userId);
        log.info("User {} removed from project {}", userId, projectId);
    }

    @Transactional
    public ProjectMemberResponse actualizarRole(UUID projectId, UUID userId, String novaRole) {
        ProjectMember member = memberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in project"));
        ProjectMemberRole memberRole = null;
        if (novaRole != null) {
            memberRole = roleRepository.findByName(novaRole).orElseThrow(() -> new ResourceNotFoundException("Função de membro não foi encontrada"));
        }
        member.setRole(memberRole);
        log.info("User {} role updated to {} in project {}", userId, novaRole, projectId);
        return ProjectMemberResponse.from(member);
    }

    @Transactional
    public ProjectMember ensureMember(Project project, User user, String roleName) {

        return memberRepository.findByProjectIdAndUserId(project.getId(), user.getId())
                .orElseGet(() -> {
                    ProjectMemberRole role = roleRepository.findByName(
                            roleName != null ? roleName : "TRABALHADOR"
                    ).orElseThrow(() -> new ResourceNotFoundException("Role not found"));

                    ProjectMember member = new ProjectMember(project, user, role);

                    log.info("User {} auto-added to project {}", user.getId(), project.getId());

                    return memberRepository.save(member);
                });
    }

    @Transactional(readOnly = true)
    public Page<ProjectMemberResponse> listarMembrosPorProjecto(UUID projectId, Pageable pageable) {
        return memberRepository.findByProjectId(projectId, pageable).map(ProjectMemberResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ProjectMemberResponse> listarProjectosPorUser(UUID userId, Pageable pageable) {
        return memberRepository.findByUserId(userId, pageable).map(ProjectMemberResponse::from);
    }

    @Transactional
    public NameAndDescriptionResponse createMemberRole(CreateCategoryRequest request) {
        if (roleRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateException("Role ja existe");
        }
        
        ProjectMemberRole newRole = new ProjectMemberRole(request.name(), request.description());
        ProjectMemberRole saved = roleRepository.save(newRole);
        return NameAndDescriptionMapper.mapToMemberRole(saved);
    }

    @Transactional
    public NameAndDescriptionResponse updateMemberRole(UUID id, CreateCategoryRequest request) {
        ProjectMemberRole role = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException());
        role.setName(request.name());
        role.setDescription(request.description());
        ProjectMemberRole saved = roleRepository.save(role);
        return NameAndDescriptionMapper.mapToMemberRole(saved);
    }

    @Transactional(readOnly = true)
    public Page<NameResponse> getMemberRoles(Pageable pageable) {
        return roleRepository.findAllRolesPage(pageable);
    } 

    @Transactional
    public void deleteMemberRole(UUID id) {
        ProjectMemberRole role = roleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Função de membro não foi encontrada"));
        roleRepository.delete(role);
        log.info("Member role deleted: {}", role);
    }
}
