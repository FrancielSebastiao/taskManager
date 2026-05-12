package com.fransebastiao.taskmanager.service;

import java.util.List;
import java.util.UUID;

import com.fransebastiao.taskmanager.domain.task.TaskComment;
import com.fransebastiao.taskmanager.dto.request.CreateCommentRequest;
import com.fransebastiao.taskmanager.dto.response.TaskCommentResponse;

public interface TaskCommentService {
    TaskComment adicionarComentario(UUID taskId, UUID authorId,
                                           CreateCommentRequest request);
    TaskComment editarComentario(UUID commentId, UUID authorId,
                                        CreateCommentRequest request);
    void eliminar(UUID commentId, UUID authorId);
    List<TaskCommentResponse> listarPorTask(UUID taskId);
    List<TaskCommentResponse> listarPorTaskECategoria(
            UUID taskId, TaskComment.CommentCategory category);
    List<TaskCommentResponse> listarPorProjecto(UUID projectId);
    TaskCommentResponse toResponse(TaskComment c);
}
