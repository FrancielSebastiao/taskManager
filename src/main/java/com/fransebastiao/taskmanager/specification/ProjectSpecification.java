package com.fransebastiao.taskmanager.specification;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.domain.project.ProjectCategory;
import com.fransebastiao.taskmanager.domain.project.ProjectMember;
import com.fransebastiao.taskmanager.dto.response.ProjectFilter;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

@Component
public class ProjectSpecification {

    public static Specification<Project> hasStatuses(List<Project.ProjectStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) return null;
            return root.get("status").in(statuses);
        };
    }

    public static Specification<Project> hasPriorities(List<Project.Priority> priorities) {
        return (root, query, cb) -> {
            if (priorities == null || priorities.isEmpty()) return null;
            return root.get("priority").in(priorities);
        };
    }

    public static Specification<Project> hasCategory(String categoryName) {
        return (root, query, cb) -> {
            if (categoryName == null || categoryName.isBlank()) return null;
            Join<Project, ProjectCategory> category = root.join("category", JoinType.LEFT);
            return cb.equal(cb.lower(category.get("name")), categoryName.toLowerCase());
        };
    }

    public static Specification<Project> hasManager(UUID managerId) {
        return (root, query, cb) -> {
            if (managerId == null) return null;
            return cb.equal(root.get("manager").get("id"), managerId);
        };
    }

    public static Specification<Project> hasDeadlineFrom(LocalDate deadlineFrom) {
        return (root, query, cb) -> {
            if (deadlineFrom == null) return null;
            return cb.greaterThanOrEqualTo(root.get("deadline"), deadlineFrom);
        };
    }

    public static Specification<Project> hasDeadlineTo(LocalDate deadlineTo) {
        return (root, query, cb) -> {
            if (deadlineTo == null) return null;
            return cb.lessThanOrEqualTo(root.get("deadline"), deadlineTo);
        };
    }

    public static Specification<Project> searchByNameOrDescription(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Filtra projetos onde o usuário está envolvido (manager ou membro da equipe)
     */
    public static Specification<Project> isInvolvedIn(UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) return null;
            
            // Usuário é manager OU é membro da equipe
            Predicate isManager = cb.equal(root.get("manager").get("id"), userId);
            
            Join<Project, ProjectMember> teamJoin = root.join("team", JoinType.LEFT);
            Predicate isTeamMember = cb.equal(teamJoin.get("user").get("id"), userId);
            
            return cb.or(isManager, isTeamMember);
        };
    }

    /**
     * Combina todos os filtros básicos (sem restrição de acesso)
     */
    public static Specification<Project> fromFilter(ProjectFilter filter) {
        return Specification
            .where(hasStatuses(filter.statuses()))
            .and(hasPriorities(filter.priorities()))
            .and(hasCategory(filter.category()))
            .and(hasManager(filter.managerId()))
            .and(hasDeadlineFrom(filter.deadlineFrom()))
            .and(hasDeadlineTo(filter.deadlineTo()))
            .and(searchByNameOrDescription(filter.search()));
    }

    /**
     * Combina filtros do usuário + restrição de acesso baseada em roles
     * 
     * LÓGICA DE ACESSO:
     * - Usuários não privilegiados: SEMPRE veem apenas projetos onde estão envolvidos
     * - Usuários privilegiados:
     *   - myProjectsOnly = true  → veem apenas projetos onde estão envolvidos
     *   - myProjectsOnly = false/null → veem todos os projetos
     */
    public static Specification<Project> fromFilterWithAccess(
            ProjectFilter filter, 
            UUID currentUserId, 
            boolean isPrivileged) {
        
        Specification<Project> filterSpec = fromFilter(filter);
        
        // Usuários não privilegiados: sempre restritos aos seus projetos
        if (!isPrivileged && currentUserId != null) {
            return filterSpec.and(isInvolvedIn(currentUserId));
        }
        
        // Usuários privilegiados: respeitam o flag myProjectsOnly
        if (isPrivileged && Boolean.TRUE.equals(filter.myProjectsOnly()) && currentUserId != null) {
            return filterSpec.and(isInvolvedIn(currentUserId));
        }
        
        // Usuários privilegiados com myProjectsOnly = false/null: veem tudo
        return filterSpec;
    }
}