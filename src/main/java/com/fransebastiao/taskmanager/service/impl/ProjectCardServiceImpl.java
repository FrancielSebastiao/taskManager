package com.fransebastiao.taskmanager.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.domain.resource.MaterialUsage;
import com.fransebastiao.taskmanager.domain.task.LaborEntry;
import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.dto.response.MemberAvatarDto;
import com.fransebastiao.taskmanager.dto.response.ProjectFilter;
import com.fransebastiao.taskmanager.dto.response.ProjectStatsResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectSummaryResponse;
import com.fransebastiao.taskmanager.dto.response.TaskBreakdownDto;
import com.fransebastiao.taskmanager.repository.LaborEntryRepository;
import com.fransebastiao.taskmanager.repository.MaterialUsageRepository;
import com.fransebastiao.taskmanager.repository.ProjectRepository;
import com.fransebastiao.taskmanager.security.CustomUserDetails;
import com.fransebastiao.taskmanager.service.ProjectCardService;
import com.fransebastiao.taskmanager.specification.ProjectSpecification;
import com.fransebastiao.taskmanager.util.AvatarHelper;
import com.fransebastiao.taskmanager.util.RoleUtils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectCardServiceImpl implements ProjectCardService {

    private final ProjectRepository       projectRepository;
    private final LaborEntryRepository    laborEntryRepository;
    private final MaterialUsageRepository materialUsageRepository;
    private final AvatarHelper            avatarHelper;

    /**
     * Busca projetos com filtros e restrições de acesso baseadas em roles
     */
    public Page<ProjectSummaryResponse> findProjects(
            ProjectFilter filter, 
            Authentication authentication) {
        
        // Obtém o usuário atual
        UUID currentUserId = getCurrentUserId(authentication);
        
        // Verifica se o usuário tem privilégios
        boolean isPrivileged = RoleUtils.isPrivileged(authentication.getAuthorities());
        
        // Cria a especificação com restrição de acesso
        Specification<Project> spec = ProjectSpecification.fromFilterWithAccess(
            filter, 
            currentUserId, 
            isPrivileged
        );
        
        // Cria o Pageable com ordenação
        Sort sort = createSort(filter.sortBy(), filter.sortDir());
        Pageable pageable = PageRequest.of(filter.page(), filter.size(), sort);
        
        // Busca os projetos
        Page<Project> projectPage = projectRepository.findAll(spec, pageable);
        
        // Verifica se o usuário tem privilégios para ver dados financeiros
        boolean canViewFinancials = isPrivileged;
        
        // Converte para DTO
        return projectPage.map(project -> toSummaryResponse(project, canViewFinancials));
    }

    /**
     * Verifica se o usuário pode acessar um projeto específico
     */
    public boolean canAccessProject(UUID projectId, Authentication authentication) {
        UUID currentUserId = getCurrentUserId(authentication);
        boolean isPrivileged = RoleUtils.isPrivileged(authentication.getAuthorities());
        
        // Privilegiados podem acessar qualquer projeto
        if (isPrivileged) {
            return true;
        }
        
        // Usuários normais só podem acessar se estiverem envolvidos (manager ou membro)
        return projectRepository.isUserInvolvedInProject(projectId, currentUserId);
    }

    /**
     * Retorna estatísticas dos projetos do usuário (útil para dashboards)
     */
    public ProjectStatsResponse getMyProjectStats(Authentication authentication) {
        UUID currentUserId = getCurrentUserId(authentication);
        boolean isPrivileged = RoleUtils.isPrivileged(authentication.getAuthorities());
        
        // Cria filtro para "meus projetos"
        ProjectFilter myProjectsFilter = new ProjectFilter(
            null, null, null, null, null, null, true,
            0, 1000, "createdAt", "desc", null
        );
        
        Specification<Project> spec = ProjectSpecification.fromFilterWithAccess(
            myProjectsFilter, currentUserId, isPrivileged
        );
        
        List<Project> myProjects = projectRepository.findAll(spec);
        
        long total = myProjects.size();
        long active = myProjects.stream()
            .filter(p -> p.getStatus() == Project.ProjectStatus.EM_PROGRESSO)
            .count();
        long planning = myProjects.stream()
            .filter(p -> p.getStatus() == Project.ProjectStatus.PLANEANDO)
            .count();
        long completed = myProjects.stream()
            .filter(p -> p.getStatus() == Project.ProjectStatus.COMPLETO)
            .count();
        long asManager = myProjects.stream()
            .filter(p -> p.getManager().getId().equals(currentUserId))
            .count();
        
        return new ProjectStatsResponse(total, active, planning, completed, asManager);
    }

    private Sort createSort(String sortBy, String sortDir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        String field = switch (sortBy) {
            case "deadline" -> "deadline";
            case "name" -> "name";
            case "createdAt" -> "createdAt";
            default -> "createdAt";
        };
        
        return Sort.by(direction, field);
    }

    private ProjectSummaryResponse toSummaryResponse(Project project, boolean canViewFinancials) {
        TaskBreakdownDto taskBreakdown = calculateTaskBreakdown(project);
        
        List<MemberAvatarDto> teamAvatars = project.getTeam().stream()
            .map(pm -> avatarHelper.toMemberAvatar(pm.getUser()))
            .distinct()
            .limit(3)
            .toList();
        
        // Dados financeiros: somente para usuários privilegiados
        BigDecimal budget = canViewFinancials ? project.getBudget() : null;
        BigDecimal spent = canViewFinancials ? project.getTotalCost() : null;
        
        return new ProjectSummaryResponse(
            project.getId(),
            project.getName(),
            project.getDescription(),
            project.getStatus().name(),
            project.getPriority().name(),
            project.getCategory() != null ? project.getCategory().getName() : null,
            project.calculateProgress(),
            project.getDeadline(),
            budget,
            spent,
            taskBreakdown,
            teamAvatars
        );
    }

    private TaskBreakdownDto calculateTaskBreakdown(Project project) {
        List<Task> tasks = project.getTasks();
        
        long total = tasks.size();
        long completed = tasks.stream()
            .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETA)
            .count();
        long inProgress = tasks.stream()
            .filter(t -> t.getStatus() == Task.TaskStatus.EM_PROGRESSO)
            .count();
        long pending = tasks.stream()
            .filter(t -> t.getStatus() == Task.TaskStatus.PENDENTE)
            .count();
        
        return new TaskBreakdownDto(total, completed, inProgress, pending);
    }

    private UUID getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        
        return null;
    }

    public ProjectSummaryResponse buscarCard(UUID projectId) {
        Project project = projectRepository.findByIdWithTeamAndTasks(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        return toCardResponse(project);
    }

    // -------------------------------------------------------------------------
    // Mapper
    // -------------------------------------------------------------------------

    public ProjectSummaryResponse toCardResponse(Project p) {
        List<Task> tasks = p.getTasks();

        long total      = tasks.size();
        long completed  = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETA).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.EM_PROGRESSO).count();
        long pending    = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.PENDENTE).count();

        // Progresso = média do progressPercent de todas as tarefas
        int progress = total == 0 ? 0 :
            (int) Math.round(tasks.stream()
                .mapToInt(Task::getProgressPercent)
                .average()
                .orElse(0));

        // Custo gasto = mão de obra concluída + materiais
        BigDecimal laborSpent = laborEntryRepository
                .findCompletedByProjectId(p.getId()).stream()
                .map(LaborEntry::calculateFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal materialSpent = materialUsageRepository
                .findByProjectIdWithMaterial(p.getId()).stream()
                .map(MaterialUsage::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal spent = laborSpent.add(materialSpent);

        // Equipa — manager + membros
        List<MemberAvatarDto> team = p.getTeam().stream()
                .map(m -> avatarHelper.toMemberAvatar(m.getUser()))
                .toList();

        return new ProjectSummaryResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getStatus().name(),
                p.getPriority().name(),
                p.getCategory().getName(),
                progress,
                p.getDeadline(),
                p.getBudget(),
                spent,
                new TaskBreakdownDto(total, completed, inProgress, pending),
                team
        );
    }
}