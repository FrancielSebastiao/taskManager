package com.fransebastiao.taskmanager.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.project.ProjectActivity;
import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.domain.task.TaskActivity;
import com.fransebastiao.taskmanager.domain.task.TaskComment;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.request.CreateCommentRequest;
import com.fransebastiao.taskmanager.dto.response.TaskCommentResponse;
import com.fransebastiao.taskmanager.exception.custom.UnauthorizedException;
import com.fransebastiao.taskmanager.repository.ProjectActivityRepository;
import com.fransebastiao.taskmanager.repository.ProjectMemberRepository;
import com.fransebastiao.taskmanager.repository.TaskActivityRepository;
import com.fransebastiao.taskmanager.repository.TaskCommentRepository;
import com.fransebastiao.taskmanager.repository.TaskRepository;
import com.fransebastiao.taskmanager.repository.UserRepository;
import com.fransebastiao.taskmanager.service.TaskCommentService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TaskCommentServiceImpl implements TaskCommentService {
    private final TaskActivityRepository    taskActivityRepository;
    private final TaskCommentRepository     commentRepository;
    private final TaskRepository            taskRepository;
    private final UserRepository            userRepository;
    private final ProjectActivityRepository projectActivityRepository;
    private final ProjectMemberRepository   memberRepository;

    public TaskComment adicionarComentario(UUID taskId, UUID authorId,
                                           CreateCommentRequest request) {
        Task task   = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        validarMembroDoProjecto(task.getProject().getId(), authorId);

        TaskComment comment = new TaskComment(
                task, author, request.category(), request.content());

        log.info("Comment added to task {} by user {}: {}",
                taskId, authorId, request.category());
        TaskComment saved = commentRepository.save(comment);

        if (task.getProject() != null) {
            projectActivityRepository.save(new ProjectActivity(
                task.getProject(), author,
                "Comentário adicionado em \"" + task.getTitle() + "\"",
                ProjectActivity.ActivityType.COMMENT_ADDED)
            );
        }

        taskActivityRepository.save(new TaskActivity(
            task, author,
            "Comentário adicionado por \"" + author.getName(),
            TaskActivity.ActivityType.COMMENT_ADDED));

        return saved;
    }

    public TaskComment editarComentario(UUID commentId, UUID authorId,
                                        CreateCommentRequest request) {
        TaskComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new UnauthorizedException("Only the author can edit this comment");
        }

        comment.editar(request.category(), request.content());
        log.info("Comment {} edited by user {}", commentId, authorId);
        return comment;
    }

    public void eliminar(UUID commentId, UUID authorId) {
        TaskComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new UnauthorizedException("Only the author can delete this comment");
        }

        commentRepository.delete(comment);
        log.info("Comment {} deleted by user {}", commentId, authorId);
    }

    @Transactional(readOnly = true)
    public List<TaskCommentResponse> listarPorTask(UUID taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskCommentResponse> listarPorTaskECategoria(
            UUID taskId, TaskComment.CommentCategory category) {
        return commentRepository.findByTaskIdAndCategory(taskId, category).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskCommentResponse> listarPorProjecto(UUID projectId) {
        return commentRepository.findByProjectId(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private void validarMembroDoProjecto(UUID projectId, UUID userId) {
        if (!memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new UnauthorizedException("User is not a member of this project");
        }
    }

    public TaskCommentResponse toResponse(TaskComment c) {
        return new TaskCommentResponse(
                c.getId(),
                c.getTask().getId(),
                c.getTask().getTitle(),
                c.getAuthor().getName(),
                c.getCategory(),
                formatarCategoria(c.getCategory()),
                c.getContent(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    private String formatarCategoria(TaskComment.CommentCategory category) {
        return switch (category) {
            case MATERIAL_SHORTAGE    -> "Material shortage";
            case WEATHER_CONDITIONS   -> "Weather conditions";
            case EQUIPMENT_FAILURE    -> "Equipment failure";
            case WAITING_FOR_APPROVAL -> "Waiting for approval";
            case OTHER                -> "Other";
        };
    }
}
