package com.fransebastiao.taskmanager.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.dto.request.UpdateProgressRequest;
import com.fransebastiao.taskmanager.dto.response.TaskProgressResponse;
import com.fransebastiao.taskmanager.security.CustomUserDetails;
import com.fransebastiao.taskmanager.service.TaskProgressService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskProgressController {

    private final TaskProgressService progressService;

    @PatchMapping("/{taskId}/progress")
    @PreAuthorize("hasAuthority('EDITAR_PROGRESSO')")
    public ResponseEntity<TaskProgressResponse> actualizarProgresso(
            @PathVariable UUID taskId,
            @RequestBody @Valid UpdateProgressRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(progressService.actualizarProgresso(taskId, userDetails.getId(), request));
    }

    @GetMapping("/{taskId}/progress")
    public ResponseEntity<TaskProgressResponse> buscarProgresso(@PathVariable UUID taskId) {
        return ResponseEntity.ok(progressService.buscarProgresso(taskId));
    }

    @GetMapping("/project/{projectId}/progress")
    public ResponseEntity<List<TaskProgressResponse>> listarProgressoPorProjecto(@PathVariable UUID projectId) {
        return ResponseEntity.ok(progressService.listarProgressoPorProjecto(projectId));
    }
}