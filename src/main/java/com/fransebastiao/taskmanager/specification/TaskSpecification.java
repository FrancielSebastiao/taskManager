package com.fransebastiao.taskmanager.specification;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.domain.task.TaskCategory;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.response.TaskFilter;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

@Component
public class TaskSpecification {

    public static Specification<Task> hasStatuses(List<Task.TaskStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) return null;
            return root.get("status").in(statuses);
        };
    }

    public static Specification<Task> hasPriorities(List<Task.Priority> priorities) {
        return (root, query, cb) -> {
            if (priorities == null || priorities.isEmpty()) return null;
            return root.get("priority").in(priorities);
        };
    }

    public static Specification<Task> hasCategory(String categoryName) {
        return (root, query, cb) -> {
            if (categoryName == null || categoryName.isBlank()) return null;
            Join<Task, TaskCategory> category = root.join("category", JoinType.LEFT);
            return cb.equal(cb.lower(category.get("name")), categoryName.toLowerCase());
        };
    }

    public static Specification<Task> hasAssignee(UUID assigneeId) {
        return (root, query, cb) -> {
            if (assigneeId == null) return null;
            Join<Task, User> assignees = root.join("assignees", JoinType.LEFT);
            return cb.equal(assignees.get("id"), assigneeId);
        };
    }

    public static Specification<Task> belongsToProject(UUID projectId) {
        return (root, query, cb) -> {
            if (projectId == null) return null;
            return cb.equal(root.get("project").get("id"), projectId);
        };
    }

    public static Specification<Task> hasDueDateFrom(LocalDate dueDateFrom) {
        return (root, query, cb) -> {
            if (dueDateFrom == null) return null;
            return cb.greaterThanOrEqualTo(root.get("dueDate"), dueDateFrom);
        };
    }

    public static Specification<Task> hasDueDateTo(LocalDate dueDateTo) {
        return (root, query, cb) -> {
            if (dueDateTo == null) return null;
            return cb.lessThanOrEqualTo(root.get("dueDate"), dueDateTo);
        };
    }

    public static Specification<Task> isOverdue(boolean overdueOnly) {
        return (root, query, cb) -> {
            if (!overdueOnly) return null;
            
            LocalDate today = LocalDate.now();
            return cb.and(
                cb.notEqual(root.get("status"), Task.TaskStatus.COMPLETA),
                cb.lessThan(root.get("dueDate"), today)
            );
        };
    }

    public static Specification<Task> searchByTitleOrDescription(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Restringe tarefas aos assignees de um usuário específico
     */
    public static Specification<Task> isAssignedTo(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;
            Join<Task, User> assignees = root.join("assignees", JoinType.LEFT);
            return cb.equal(assignees.get("id"), userId);
        };
    }

    /**
     * Combina todos os filtros básicos (sem restrição de acesso)
     */
    public static Specification<Task> fromFilter(TaskFilter filter) {
        return Specification
            .where(hasStatuses(filter.statuses()))
            .and(hasPriorities(filter.priorities()))
            .and(hasCategory(filter.category()))
            .and(hasAssignee(filter.assigneeId()))
            .and(belongsToProject(filter.projectId()))
            .and(hasDueDateFrom(filter.dueDateFrom()))
            .and(hasDueDateTo(filter.dueDateTo()))
            .and(isOverdue(filter.overdueOnly()))
            .and(searchByTitleOrDescription(filter.search()));
    }

    /**
     * Combina filtros do usuário + restrição de acesso baseada em roles
     * 
     * LÓGICA DE ACESSO:
     * - Usuários não privilegiados: SEMPRE veem apenas suas tarefas
     * - Usuários privilegiados:
     *   - myTasksOnly = true  → veem apenas suas tarefas
     *   - myTasksOnly = false/null → veem todas as tarefas
     */
    public static Specification<Task> fromFilterWithAccess(
            TaskFilter filter, 
            UUID currentUserId, 
            boolean isPrivileged) {
        
        Specification<Task> filterSpec = fromFilter(filter);
        
        // Usuários não privilegiados: sempre restritos às suas tarefas
        if (!isPrivileged && currentUserId != null) {
            return filterSpec.and(isAssignedTo(currentUserId));
        }
        
        // Usuários privilegiados: respeitam o flag myTasksOnly
        if (isPrivileged && Boolean.TRUE.equals(filter.myTasksOnly()) && currentUserId != null) {
            return filterSpec.and(isAssignedTo(currentUserId));
        }
        
        // Usuários privilegiados com myTasksOnly = false/null: veem tudo
        return filterSpec;
    }
}