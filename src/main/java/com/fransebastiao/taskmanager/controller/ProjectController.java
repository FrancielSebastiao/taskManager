package com.fransebastiao.taskmanager.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.request.CreateProjectRequest;
import com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse;
import com.fransebastiao.taskmanager.dto.response.PagedResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectDashboardResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectFilter;
import com.fransebastiao.taskmanager.dto.response.ProjectResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectStatsResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectSummaryResponse;
import com.fransebastiao.taskmanager.repository.UserRepository;
import com.fransebastiao.taskmanager.security.CustomUserDetails;
import com.fransebastiao.taskmanager.service.ProjectCardService;
import com.fransebastiao.taskmanager.service.ProjectDashboardStatsService;
import com.fransebastiao.taskmanager.service.ProjectService;
import com.fransebastiao.taskmanager.util.RoleUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Validated
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectCardService projectCardService;
    private final ProjectDashboardStatsService dashboardStatsService;
    private final UserRepository        userRepository;

    @GetMapping("/{id}/card")
    @PreAuthorize("hasAuthority('LER_PROJECTOS')")
    public ResponseEntity<ProjectSummaryResponse> buscarCard(@PathVariable UUID id) {
        return ResponseEntity.ok(projectCardService.buscarCard(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CRIAR_PROJECTOS')")
    public ResponseEntity<ProjectResponse> criar(@RequestBody @Valid CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDITAR_PROJECTOS')")
    public ResponseEntity<ProjectResponse> update(@PathVariable UUID id, @RequestBody @Valid CreateProjectRequest request) {
        return ResponseEntity.ok(projectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('APAGAR_PROJECTOS')")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProjectDashboardResponse> getStats(
            @AuthenticationPrincipal CustomUserDetails userDetails
        ) {

        boolean isPrivileged = RoleUtils.isPrivileged(userDetails.getAuthorities());

        User user = userRepository.findByIdWithRolesAndPrivileges(userDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return ResponseEntity.ok(dashboardStatsService.getStats(user, isPrivileged));
    }

    @GetMapping("/names")
    public ResponseEntity<List<NameAndDescriptionResponse>> getProjectNames() {
        return ResponseEntity.ok(projectService.getProjectNameAndDescription());
    }

    @GetMapping("/cards")
    @PreAuthorize("hasAuthority('LER_PROJECTOS')")
    public PagedResponse<ProjectSummaryResponse> listProjects(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> priorities,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID managerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineTo,
            @RequestParam(required = false) Boolean myProjectsOnly,  // ← NOVO PARÂMETRO
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        
        List<Project.ProjectStatus> statusEnums = parseStatuses(statuses);
        List<Project.Priority> priorityEnums = parsePriorities(priorities);
        
        ProjectFilter filter = new ProjectFilter(
            statusEnums,
            priorityEnums,
            category,
            managerId,
            deadlineFrom,
            deadlineTo,
            myProjectsOnly,  // ← PASSADO AO FILTRO
            page,
            size,
            sortBy,
            sortDir,
            search
        );
        
        Page<ProjectSummaryResponse> result = projectCardService.findProjects(filter, authentication);
        return PagedResponse.of(result);
    }

    /**
     * Endpoint adicional: retorna apenas os projetos do usuário logado
     * Útil para "My Projects" dashboard
     */
    @GetMapping("/my-projects")
    @PreAuthorize("hasAuthority('LER_PROJECTOS')")
    public PagedResponse<ProjectSummaryResponse> getMyProjects(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> priorities,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "deadline") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {
        
        List<Project.ProjectStatus> statusEnums = parseStatuses(statuses);
        List<Project.Priority> priorityEnums = parsePriorities(priorities);
        
        // Força myProjectsOnly = true
        ProjectFilter filter = new ProjectFilter(
            statusEnums,
            priorityEnums,
            null,
            null,
            null,
            null,
            true,  // ← SEMPRE meus projetos
            page,
            size,
            sortBy,
            sortDir,
            null
        );
        
        Page<ProjectSummaryResponse> result = projectCardService.findProjects(filter, authentication);
        return PagedResponse.of(result);
    }

    /**
     * Endpoint adicional: estatísticas dos projetos do usuário
     */
    @GetMapping("/my-stats")
    @PreAuthorize("hasAuthority('LER_PROJECTOS')")
    public ResponseEntity<ProjectStatsResponse> getMyProjectStats(Authentication authentication) {
        ProjectStatsResponse stats = projectCardService.getMyProjectStats(authentication);
        return ResponseEntity.ok(stats);
    }

    /**
     * Endpoint adicional: retorna apenas projetos onde o usuário é manager
     */
    @GetMapping("/managed-by-me")
    @PreAuthorize("hasAuthority('LER_PROJECTOS')")
    public PagedResponse<ProjectSummaryResponse> getManagedProjects(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Authentication authentication) {
        
        UUID currentUserId = getCurrentUserId(authentication);
        List<Project.ProjectStatus> statusEnums = parseStatuses(statuses);
        
        // Filtra por managerId = currentUserId
        ProjectFilter filter = new ProjectFilter(
            statusEnums,
            null,
            null,
            currentUserId,  // ← Filtra pelo ID do usuário atual
            null,
            null,
            null,
            page,
            size,
            sortBy,
            sortDir,
            null
        );
        
        Page<ProjectSummaryResponse> result = projectCardService.findProjects(filter, authentication);
        return PagedResponse.of(result);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasAuthority('LER_PROJECTOS')")
    public ResponseEntity<ProjectSummaryResponse> getProject(
            @PathVariable UUID projectId,
            Authentication authentication) {
        
        if (!projectCardService.canAccessProject(projectId, authentication)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Implementar lógica de busca individual
        // ...
        
        return ResponseEntity.ok().build();
    }

    private List<Project.ProjectStatus> parseStatuses(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) return null;
        return statuses.stream()
            .map(s -> Project.ProjectStatus.valueOf(s.toUpperCase()))
            .toList();
    }

    private List<Project.Priority> parsePriorities(List<String> priorities) {
        if (priorities == null || priorities.isEmpty()) return null;
        return priorities.stream()
            .map(p -> Project.Priority.valueOf(p.toUpperCase()))
            .toList();
    }

    private UUID getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User userDetails) {
            return userDetails.getId();
        }
        
        return null;
    }
}

