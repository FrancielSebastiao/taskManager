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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.dto.request.CreateTaskRequest;
import com.fransebastiao.taskmanager.dto.response.AssigneeAvatarDto;
import com.fransebastiao.taskmanager.dto.response.PagedResponse;
import com.fransebastiao.taskmanager.dto.response.TaskFilter;
import com.fransebastiao.taskmanager.dto.response.TaskResponse;
import com.fransebastiao.taskmanager.dto.response.TaskStatsResponse;
import com.fransebastiao.taskmanager.dto.response.TaskSummaryResponse;
import com.fransebastiao.taskmanager.security.CustomUserDetails;
import com.fransebastiao.taskmanager.service.TaskCardService;
import com.fransebastiao.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Validated
public class TaskController {

    private final TaskService       taskService;
    private final TaskCardService   taskCardService;

    @PostMapping
    @PreAuthorize("hasAuthority('CRIAR_TAREFAS')")
    public ResponseEntity<TaskResponse> criar(
        @RequestBody @Valid CreateTaskRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.criar(request, userDetails.getId()));
    }

    @PostMapping("/{id}/subtask")
    @PreAuthorize("hasAuthority('CRIAR_TAREFAS')")
    public ResponseEntity<TaskResponse> criarSubtarefa(
        @PathVariable UUID id,
        @RequestBody @Valid CreateTaskRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.criarSubTarefa(id, request, userDetails.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EDITAR_TAREFAS')")
    public ResponseEntity<TaskResponse> update(
        @PathVariable UUID id, 
        @RequestBody @Valid CreateTaskRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(taskService.criar(request, userDetails.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LER_TAREFAS')")
    public ResponseEntity<TaskResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.buscarPorId(id));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResponse>> listarPorProjecto(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.listarPorProjecto(projectId));
    }

    @GetMapping("/project/{projectId}/status/{status}")
    public ResponseEntity<List<TaskResponse>> listarPorProjectoEStatus(
            @PathVariable UUID projectId,
            @PathVariable Task.TaskStatus status) {
        return ResponseEntity.ok(taskService.listarPorProjectoEStatus(projectId, status));
    }

    @GetMapping("/assignee/{userId}")
    public ResponseEntity<List<TaskResponse>> listarPorAssignee(@PathVariable UUID userId) {
        return ResponseEntity.ok(taskService.listarPorAssignee(userId));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponse>> listarEmAtraso() {
        return ResponseEntity.ok(taskService.listarEmAtraso());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> actualizarStatus(
            @PathVariable UUID id,
            @RequestParam Task.TaskStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails
        ) {
        return ResponseEntity.ok(taskService.actualizarStatus(id, status, userDetails.getId()));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> concluir(
        @PathVariable UUID id,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(taskService.concluir(id, userDetails.getId()));
    }

    @PostMapping("/{id}/assignees/{userId}")
    public ResponseEntity<TaskResponse> adicionarAssignee(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(taskService.adicionarAssignee(id, userId));
    }

    @PostMapping("/{id}/assignees")
    public ResponseEntity<TaskResponse> adicionarAssignees(
            @PathVariable UUID id,
            @RequestBody List<UUID> userIds) {
        return ResponseEntity.ok(taskService.adicionarAssignees(id, userIds));
    }

    @DeleteMapping("/{id}/assignees/{userId}")
    public ResponseEntity<Void> removerAssignee(
            @PathVariable UUID id,
            @PathVariable UUID userId) {
        
        taskService.removerMembro(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('APAGAR_TAREFAS')")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        taskService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cards")
    public PagedResponse<TaskSummaryResponse> listTasks(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> priorities,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDateTo,
            @RequestParam(defaultValue = "false") boolean overdueOnly,
            @RequestParam(required = false) Boolean myTasksOnly,  // ← NOVO PARÂMETRO
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        
        List<Task.TaskStatus> statusEnums = parseStatuses(statuses);
        List<Task.Priority> priorityEnums = parsePriorities(priorities);
        
        TaskFilter filter = new TaskFilter(
            statusEnums,
            priorityEnums,
            category,
            assigneeId,
            projectId,
            dueDateFrom,
            dueDateTo,
            overdueOnly,
            myTasksOnly,  // ← PASSADO AO FILTRO
            page,
            size,
            sortBy,
            sortDir,
            search
        );
        
        Page<TaskSummaryResponse> result = taskCardService.findTasks(filter, authentication);
        return PagedResponse.of(result);
    }

    /**
     * Endpoint adicional: retorna apenas as tarefas do usuário logado
     * Útil para "My Tasks" dashboard
     */
    @GetMapping("/my-tasks")
    public PagedResponse<TaskSummaryResponse> getMyTasks(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) List<String> priorities,
            @RequestParam(defaultValue = "false") boolean overdueOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication) {
        
        List<Task.TaskStatus> statusEnums = parseStatuses(statuses);
        List<Task.Priority> priorityEnums = parsePriorities(priorities);
        
        // Força myTasksOnly = true
        TaskFilter filter = new TaskFilter(
            statusEnums,
            priorityEnums,
            null,
            null,
            null,
            null,
            null,
            overdueOnly,
            true,  // ← SEMPRE minhas tarefas
            page,
            size,
            sortBy,
            sortDir,
            null
        );
        
        Page<TaskSummaryResponse> result = taskCardService.findTasks(filter, authentication);
        return PagedResponse.of(result);
    }

    /**
     * Endpoint adicional: estatísticas das tarefas do usuário
     */
    @GetMapping("/my-stats")
    public ResponseEntity<TaskStatsResponse> getMyTaskStats(Authentication authentication) {
        TaskStatsResponse stats = taskCardService.getMyTaskStats(authentication);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}/available-members")
    public ResponseEntity<List<AssigneeAvatarDto>> getAvailableMembers(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getAvailableMembers(id));
    }

    // @GetMapping("/{taskId}")
    // public ResponseEntity<TaskSummaryResponse> getTask(
    //         @PathVariable UUID taskId,
    //         Authentication authentication) {
        
    //     if (!taskCardService.canAccessTask(taskId, authentication)) {
    //         return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    //     }
        
    //     // Implementar lógica de busca individual
    //     // ...
        
    //     return ResponseEntity.ok().build();
    // }

    private List<Task.TaskStatus> parseStatuses(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) return null;
        return statuses.stream()
            .map(s -> Task.TaskStatus.valueOf(s.toUpperCase()))
            .toList();
    }

    private List<Task.Priority> parsePriorities(List<String> priorities) {
        if (priorities == null || priorities.isEmpty()) return null;
        return priorities.stream()
            .map(p -> Task.Priority.valueOf(p.toUpperCase()))
            .toList();
    }
}
