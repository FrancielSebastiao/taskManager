package com.fransebastiao.taskmanager.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.domain.project.ProjectActivity;
import com.fransebastiao.taskmanager.domain.project.ProjectFile;
import com.fransebastiao.taskmanager.domain.project.ProjectMember;
import com.fransebastiao.taskmanager.domain.resource.MaterialUsage;
import com.fransebastiao.taskmanager.domain.task.LaborEntry;
import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.response.ActivityDto;
import com.fransebastiao.taskmanager.dto.response.PagedResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectDetailResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectFileDto;
import com.fransebastiao.taskmanager.dto.response.TaskBreakdownDto;
import com.fransebastiao.taskmanager.dto.response.TaskSummaryDto;
import com.fransebastiao.taskmanager.dto.response.TeamMemberDetailDto;
import com.fransebastiao.taskmanager.exception.custom.ResourceNotFoundException;
import com.fransebastiao.taskmanager.repository.LaborEntryRepository;
import com.fransebastiao.taskmanager.repository.MaterialUsageRepository;
import com.fransebastiao.taskmanager.repository.ProjectActivityRepository;
import com.fransebastiao.taskmanager.repository.ProjectFileRepository;
import com.fransebastiao.taskmanager.repository.ProjectMemberRepository;
import com.fransebastiao.taskmanager.repository.ProjectRepository;
import com.fransebastiao.taskmanager.repository.TaskRepository;
import com.fransebastiao.taskmanager.service.ProjectDetailService;
import com.fransebastiao.taskmanager.service.S3Service;
import com.fransebastiao.taskmanager.util.AvatarHelper;
import com.fransebastiao.taskmanager.util.RelativeDateHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectDetailServiceImpl implements ProjectDetailService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final ProjectActivityRepository activityRepository;
    private final ProjectFileRepository fileRepository;
    private final LaborEntryRepository laborEntryRepository;
    private final MaterialUsageRepository materialUsageRepository;
    private final S3Service s3Service;
    private final AvatarHelper avatarHelper;
    private final RelativeDateHelper relativeDateHelper;
    private static final int DEFAULT_PAGE_SIZE = 5;

    public ProjectDetailResponse getDetail(UUID projectId) {
        Project project = projectRepository.findByIdWithTeamAndTasks(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        return buildResponse(project);
    }

    // Endpoints de paginação separados — chamados pelo "Ver mais"
    public PagedResponse<TaskSummaryDto> getTasks(UUID projectId, int page) {
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE,
            Sort.by("createdAt").descending());

        return PagedResponse.of(
            taskRepository.findByProjectId(projectId, pageable)
            .map(this::toTaskSummary));
    }

    public PagedResponse<ActivityDto> getActivities(UUID projectId, int page) {
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        return PagedResponse.of(
            activityRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
            .map(this::toActivityDto));
    }

    public PagedResponse<TeamMemberDetailDto> getTeam(UUID projectId, int page) {
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        return PagedResponse.of(
            projectMemberRepository.findTeamByProjectId(projectId, pageable)
            .map(this::toTeamMemberDto));
    }

    public PagedResponse<ProjectFileDto> getFiles(UUID projectId, int page) {
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        return PagedResponse.of(
            fileRepository.findByProjectIdOrderByUploadedAtDesc(projectId, pageable)
            .map(this::toFileDto));
    }

    // -------------------------------------------------------------------------
    // Mappers privados
    // -------------------------------------------------------------------------
    private ProjectDetailResponse buildResponse(Project project) {
        List<Task> tasks = project.getTasks();

        long total      = tasks.size();
        long completed  = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETA).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.EM_PROGRESSO).count();
        long pending    = tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.PENDENTE).count();

        BigDecimal laborSpent = laborEntryRepository.findCompletedByProjectId(project.getId())
            .stream().map(LaborEntry::calculateFinalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal materialSpent = materialUsageRepository
            .findByProjectIdWithMaterial(project.getId())
            .stream().map(MaterialUsage::getTotalCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Primeira página de cada secção
        Pageable first = PageRequest.of(0, DEFAULT_PAGE_SIZE);

        return new ProjectDetailResponse(
            project.getId(),
            project.getName(),
            project.getDescription(),
            project.getStatus().name(),
            project.getPriority().name(),
            project.getCategory().getName(),
            project.calculateProgress(),
            project.getStartDate(),
            project.getDeadline(),
            project.getBudget(),
            laborSpent.add(materialSpent),
            resolveIconBgClass(project.getCategory().getName()),
            resolveIconColorClass(project.getCategory().getName()),
            new TaskBreakdownDto(total, completed, inProgress, pending),
            avatarHelper.toMemberAvatar(project.getManager()),
            PagedResponse.of(
                taskRepository.findByProjectId(project.getId(), first)
                .map(this::toTaskSummary)
            ),
            PagedResponse.of(
                activityRepository.findByProjectIdOrderByCreatedAtDesc(project.getId(), first)
                .map(this::toActivityDto)
            ),
            PagedResponse.of(
                projectMemberRepository.findTeamByProjectId(project.getId(), first)
                .map(this::toTeamMemberDto)
            ),
            PagedResponse.of(
                fileRepository.findByProjectIdOrderByUploadedAtDesc(project.getId(), first)
                .map(this::toFileDto)
            )
        );
    }

    private TaskSummaryDto toTaskSummary(Task t) {
        User firstAssignee = t.getAssignees().isEmpty() ? null
            : t.getAssignees().iterator().next();
        
        return new TaskSummaryDto(
            t.getId(),
            t.getTitle(),
            t.getStatus() == Task.TaskStatus.COMPLETA,
            firstAssignee != null ? avatarHelper.initials(firstAssignee.getName()) : "?",
            firstAssignee != null ? avatarHelper.color(firstAssignee.getName()) : "#888",
            relativeDateHelper.fromDate(t.getDueDate()),
            t.getPriority().name()
        );
    }

    private ActivityDto toActivityDto(ProjectActivity a) {
        return new ActivityDto(
            a.getId(),
            a.getText(),
            a.getUser() != null ? a.getUser().getName() : "Sistema",
            relativeDateHelper.fromDateTime(a.getCreatedAt()),
            resolveMarkerClass(a.getType())
        );
    }

    private TeamMemberDetailDto toTeamMemberDto(ProjectMember m) {
        User user = m.getUser();
        return new TeamMemberDetailDto(
            user.getId(),
            avatarHelper.initials(user.getName()),
            user.getName(),
            m.getRole().getName(),
            avatarHelper.color(user.getName())
        );
    }

    private ProjectFileDto toFileDto(ProjectFile f) {
        return new ProjectFileDto(
            f.getId(),
            f.getOriginalName(),
            f.formatSize(),
            f.resolveIcon(),
            f.resolveIconBgClass(),
            f.resolveIconColorClass(),
            s3Service.gerarUrlPresignada(f.getS3Key())
        );
    }
    
    private String resolveMarkerClass(ProjectActivity.ActivityType type) {
        return switch (type) {
            case TASK_COMPLETED -> "marker--green";
            case FILE_ADDED -> "marker--blue";
            case COMMENT_ADDED -> "marker--purple";
            case DEADLINE_UPDATED,
            STATUS_CHANGED -> "marker--amber";
            default -> "marker--gray";
        };
    }

    private String resolveIconBgClass(String category) {
        if (category == null) return "icon-bg--gray";
        return switch (category.toLowerCase()) {
            case "estrutural" -> "icon-bg--blue";
            case "hidráulica" -> "icon-bg--teal";
            case "elétrica" -> "icon-bg--amber";
            case "acabamentos" -> "icon-bg--pink";
            case "inspeção" -> "icon-bg--purple";
            case "terraplanagem"-> "icon-bg--green";
            default -> "icon-bg--gray";
        };
    }
    
    private String resolveIconColorClass(String category) {
        return resolveIconBgClass(category).replace("bg--", "--");
    }   
}
