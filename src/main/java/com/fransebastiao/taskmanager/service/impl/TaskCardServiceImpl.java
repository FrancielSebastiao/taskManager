package com.fransebastiao.taskmanager.service.impl;

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

import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.dto.response.AssigneeAvatarDto;
import com.fransebastiao.taskmanager.dto.response.TaskFilter;
import com.fransebastiao.taskmanager.dto.response.TaskStatsResponse;
import com.fransebastiao.taskmanager.dto.response.TaskSummaryResponse;
import com.fransebastiao.taskmanager.repository.TaskRepository;
import com.fransebastiao.taskmanager.security.CustomUserDetails;
import com.fransebastiao.taskmanager.service.TaskCardService;
import com.fransebastiao.taskmanager.specification.TaskSpecification;
import com.fransebastiao.taskmanager.util.AvatarHelper;
import com.fransebastiao.taskmanager.util.RoleUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskCardServiceImpl implements TaskCardService {

    private final TaskRepository taskRepository;
    private final AvatarHelper   avatarHelper;

    /**
     * Busca tarefas com filtros e restrições de acesso baseadas em roles
     */
    public Page<TaskSummaryResponse> findTasks(
            TaskFilter filter, 
            Authentication authentication) {
        
        // Obtém o usuário atual
        UUID currentUserId = getCurrentUserId(authentication);
        
        // Verifica se o usuário tem privilégios
        boolean isPrivileged = RoleUtils.isPrivileged(authentication.getAuthorities());
        
        // Cria a especificação com restrição de acesso
        Specification<Task> spec = TaskSpecification.fromFilterWithAccess(
            filter, 
            currentUserId, 
            isPrivileged
        );
        
        // Cria o Pageable com ordenação
        Sort sort = createSort(filter.sortBy(), filter.sortDir());
        Pageable pageable = PageRequest.of(filter.page(), filter.size(), sort);
        
        // Busca as tarefas
        Page<Task> taskPage = taskRepository.findAll(spec, pageable);
        
        // Converte para DTO
        return taskPage.map(this::toSummaryResponse);
    }

    /**
     * Verifica se o usuário pode acessar uma tarefa específica
     */
    public boolean canAccessTask(UUID taskId, Authentication authentication) {
        UUID currentUserId = getCurrentUserId(authentication);
        boolean isPrivileged = RoleUtils.isPrivileged(authentication.getAuthorities());
        
        // Privilegiados podem acessar qualquer tarefa
        if (isPrivileged) {
            return true;
        }
        
        // Usuários normais só podem acessar se forem assignees
        return taskRepository.isUserAssignedToTask(taskId, currentUserId);
    }

    /**
     * Retorna estatísticas das tarefas do usuário (útil para dashboards)
     */
    public TaskStatsResponse getMyTaskStats(Authentication authentication) {
        UUID currentUserId = getCurrentUserId(authentication);
        boolean isPrivileged = RoleUtils.isPrivileged(authentication.getAuthorities());
        
        // Cria filtro para "minhas tarefas"
        TaskFilter myTasksFilter = new TaskFilter(
            null, null, null, null, null, null, null, false, true,
            0, 1000, "createdAt", "desc", null
        );
        
        Specification<Task> spec = TaskSpecification.fromFilterWithAccess(
            myTasksFilter, currentUserId, isPrivileged
        );
        
        List<Task> myTasks = taskRepository.findAll(spec);
        
        long total = myTasks.size();
        long completed = myTasks.stream()
            .filter(t -> t.getStatus() == Task.TaskStatus.COMPLETA)
            .count();
        long inProgress = myTasks.stream()
            .filter(t -> t.getStatus() == Task.TaskStatus.EM_PROGRESSO)
            .count();
        long overdue = myTasks.stream()
            .filter(Task::isOverdue)
            .count();
        
        return new TaskStatsResponse(total, completed, inProgress, overdue);
    }

    private Sort createSort(String sortBy, String sortDir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        String field = switch (sortBy) {
            case "dueDate" -> "dueDate";
            case "title" -> "title";
            case "status" -> "status";
            case "priority" -> "priority";
            case "createdAt" -> "createdAt";
            case "progressPercent" -> "progressPercent";
            default -> "createdAt";
        };
        
        return Sort.by(direction, field);
    }

    private TaskSummaryResponse toSummaryResponse(Task task) {
        List<AssigneeAvatarDto> assigneeAvatars = task.getAssignees().stream()
            .map(avatarHelper::toAssigneeAvatar)
            .sorted((a, b) -> a.name().compareTo(b.name()))
            .toList();
        
        return new TaskSummaryResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus().name(),
            task.getPriority().name(),
            task.getProgressPercent(),
            task.getDueDate(),
            task.isOverdue(),
            assigneeAvatars
        );
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
}
