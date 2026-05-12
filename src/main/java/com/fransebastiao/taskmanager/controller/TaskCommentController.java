package com.fransebastiao.taskmanager.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.domain.task.TaskComment;
import com.fransebastiao.taskmanager.dto.request.CreateCommentRequest;
import com.fransebastiao.taskmanager.dto.response.TaskCommentResponse;
import com.fransebastiao.taskmanager.security.CustomUserDetails;
import com.fransebastiao.taskmanager.service.TaskCommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService commentService;

    @PostMapping("/{taskId}/comment")
    @PreAuthorize("hasAuthority('CRIAR_COMENTARIOS')")
    public ResponseEntity<TaskCommentResponse> adicionar(
            @PathVariable UUID taskId,
            @RequestBody @Valid CreateCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentService.toResponse(
                        commentService.adicionarComentario(taskId, userDetails.getId(), request)));
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<TaskCommentResponse>> listarPorTask(
            @PathVariable UUID taskId) {
        return ResponseEntity.ok(commentService.listarPorTask(taskId));
    }

    @GetMapping("/{taskId}/comments/category/{category}")
    public ResponseEntity<List<TaskCommentResponse>> listarPorCategoria(
            @PathVariable UUID taskId,
            @PathVariable TaskComment.CommentCategory category) {
        return ResponseEntity.ok(commentService.listarPorTaskECategoria(taskId, category));
    }

    @PutMapping("/{taskId}/{commentId}")
    @PreAuthorize("hasAuthority('EDITAR_COMENTARIOS')")
    public ResponseEntity<TaskCommentResponse> editar(
            @PathVariable UUID taskId,
            @PathVariable UUID commentId,
            @RequestBody @Valid CreateCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(commentService.toResponse(commentService.editarComentario(commentId, userDetails.getId(), request)));
    }

    @DeleteMapping("/{taskId}/{commentId}")
    @PreAuthorize("hasAuthority('APAGAR_COMENTARIOS')")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID taskId,
            @PathVariable UUID commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        commentService.eliminar(commentId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
