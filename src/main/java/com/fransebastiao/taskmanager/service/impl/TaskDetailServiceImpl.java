package com.fransebastiao.taskmanager.service.impl;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.attachment.TaskPhoto;
import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.domain.task.TaskActivity;
import com.fransebastiao.taskmanager.domain.task.TaskComment;
import com.fransebastiao.taskmanager.domain.task.TaskFile;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.response.AssigneeAvatarDto;
import com.fransebastiao.taskmanager.dto.response.AssigneeDetailDto;
import com.fransebastiao.taskmanager.dto.response.PagedResponse;
import com.fransebastiao.taskmanager.dto.response.TaskActivityDto;
import com.fransebastiao.taskmanager.dto.response.TaskCommentDetailDto;
import com.fransebastiao.taskmanager.dto.response.TaskDetailResponse;
import com.fransebastiao.taskmanager.dto.response.TaskFileDto;
import com.fransebastiao.taskmanager.dto.response.TaskImageDto;
import com.fransebastiao.taskmanager.exception.custom.ResourceNotFoundException;
import com.fransebastiao.taskmanager.repository.ProjectMemberRepository;
import com.fransebastiao.taskmanager.repository.TaskActivityRepository;
import com.fransebastiao.taskmanager.repository.TaskCommentRepository;
import com.fransebastiao.taskmanager.repository.TaskFileRepository;
import com.fransebastiao.taskmanager.repository.TaskPhotoRepository;
import com.fransebastiao.taskmanager.repository.TaskRepository;
import com.fransebastiao.taskmanager.service.S3Service;
import com.fransebastiao.taskmanager.service.TaskDetailService;
import com.fransebastiao.taskmanager.util.AvatarHelper;
import com.fransebastiao.taskmanager.util.RelativeDateHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskDetailServiceImpl implements TaskDetailService {
    private final TaskRepository            taskRepository;
    private final TaskFileRepository        taskFileRepository;
    private final TaskPhotoRepository       taskPhotoRepository;
    private final TaskActivityRepository    taskActivityRepository;
    private final TaskCommentRepository     taskCommentRepository;
    private final ProjectMemberRepository   memberRepository;
    private final S3Service                 s3Service;
    private final AvatarHelper              avatarHelper;
    private final RelativeDateHelper        relativeDateHelper;
    private static final int DEFAULT_PAGE_SIZE = 5;

    public TaskDetailResponse getDetail(UUID taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        return buildResponse(task);
    }

    public PagedResponse<AssigneeDetailDto> getTaskAssigness(UUID taskId, int page) {
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return PagedResponse.of(taskRepository.findAssigneesByTaskId(task.getId(), pageable).map(user -> toAssigneeDetailDto(task, user)));
    }

    public PagedResponse<TaskImageDto> getTaskImages(UUID taskId, int page) {
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        return PagedResponse.of(taskPhotoRepository.findTaskPhotos(taskId, pageable).map(this::toTaskImageDto));
    }

    public PagedResponse<TaskFileDto> getTaskFiles(UUID taskId, int page) {
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        return PagedResponse.of(taskFileRepository.findByTaskIdOrderByUploadedAtDesc(taskId, pageable).map(this::toTaskFileDto));
    }

    public PagedResponse<TaskCommentDetailDto> getTaskComments(UUID taskId, int page) {
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        return PagedResponse.of(taskCommentRepository.findByTaskId(taskId, pageable).map(this::toTaskCommentDetailDto));
    }

    public PagedResponse<TaskActivityDto> getTaskActivities(UUID taskId, int page) {
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        return PagedResponse.of(taskActivityRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageable).map(this::toTaskActivityDto));
    }

    // -------------------------------------------------------------------------
    // Mappers privados
    // -------------------------------------------------------------------------
    private TaskDetailResponse buildResponse(Task task) {
        AssigneeAvatarDto createdBy = task.getCreatedBy() != null
            ? avatarHelper.toAssigneeAvatar(task.getCreatedBy())
            : null;

        Pageable first = PageRequest.of(0, DEFAULT_PAGE_SIZE);

        return new TaskDetailResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus().name(),
            task.getPriority().name(),
            task.getCategory() != null ? task.getCategory().getName() : null,
            task.getProgressPercent(),
            task.getDueDate(),
            relativeDateHelper.fromDate(task.getDueDate()),
            task.getCreatedAt().toLocalDate(),
            task.getEstimatedHours(),
            task.getProject() != null ? task.getProject().getName() : null,
            createdBy,
            PagedResponse.of(
                taskRepository.findAssigneesByTaskId(task.getId(), first)
                    .map(user -> toAssigneeDetailDto(task, user))
            ),
            PagedResponse.of(
                taskPhotoRepository.findTaskPhotos(task.getId(), first)
                    .map(taskPhoto -> toTaskImageDto(taskPhoto))
            ),
            PagedResponse.of(
                taskFileRepository.findByTaskIdOrderByUploadedAtDesc(task.getId(), first)
                    .map(this::toTaskFileDto)
            ),
            PagedResponse.of(
                taskCommentRepository.findByTaskId(task.getId(), first)
                    .map(this::toTaskCommentDetailDto)
            ),
            PagedResponse.of(
                taskActivityRepository.findByTaskIdOrderByCreatedAtDesc(task.getId(), first)
                    .map(this::toTaskActivityDto)
            )
        );
    }

    private TaskImageDto toTaskImageDto(TaskPhoto p) {
        return new TaskImageDto(
            p.getId(),
            s3Service.gerarUrlPresignada(p.getS3Key()),
            p.getS3Key().substring(p.getS3Key().lastIndexOf('/') + 1),
            relativeDateHelper.fromDateTime(p.getUploadedAt()),
            avatarHelper.initials(p.getUploadedBy().getName()),
            avatarHelper.color(p.getUploadedBy().getName())
        );
    }

    private AssigneeDetailDto toAssigneeDetailDto(Task task, User user) {
        String role;
        if (task.getProject() != null) {
            role = memberRepository.findByProjectIdAndUserId(task.getProject().getId(), user.getId())
                .map(m -> m.getRole().getName())
                .orElse("TRABALHADOR");
        } else {
            role = "TRABALHADOR";
        }

        return new AssigneeDetailDto(
            user.getId(),
            avatarHelper.initials(user.getName()),
            user.getName(),
            role,
            avatarHelper.color(user.getName()),
            user.getEmail()
        );
    }

    private TaskFileDto toTaskFileDto(TaskFile f) {
        return new TaskFileDto(
            f.getId(),
            f.getOriginalName(),
            f.formatSize(),
            f.resolveIcon(),
            f.resolveIconBgClass(),
            f.resolveIconColorClass(),
            s3Service.gerarUrlPresignada(f.getS3Key())
        );
    }

    private TaskCommentDetailDto toTaskCommentDetailDto(TaskComment c) {
        return new TaskCommentDetailDto(
            c.getId(),
            c.getAuthor().getName(),
            avatarHelper.initials(c.getAuthor().getName()),
            avatarHelper.color(c.getAuthor().getName()),
            c.getContent(),
            formatCategory(c.getCategory()),
            relativeDateHelper.fromDateTime(c.getCreatedAt()),
            c.getAttachmentFile() != null ? c.getAttachmentFile().getOriginalName() : null,
            c.getAttachmentFile() != null ? s3Service.gerarUrlPresignada(c.getAttachmentFile().getS3Key()) : null
        );
    }

    private TaskActivityDto toTaskActivityDto(TaskActivity a) {
        return new TaskActivityDto(
            a.getId(),
            a.getText(),
            a.getUser() != null ? a.getUser().getName() : "Sistema",
            relativeDateHelper.fromDateTime(a.getCreatedAt()),
            a.resolveMarkerClass()
        );
    }

    private String formatCategory(TaskComment.CommentCategory category) {
        if (category == null) return null;
            return switch (category) {
                case MATERIAL_SHORTAGE      -> "Material shortage";
                case WEATHER_CONDITIONS     -> "Weather conditions";
                case EQUIPMENT_FAILURE      -> "Equipment failure";
                case WAITING_FOR_APPROVAL   -> "Waiting for approval";
                case OTHER                  -> "Other";
        };
    }
}
