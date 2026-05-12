package com.fransebastiao.taskmanager.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.domain.project.ProjectActivity;
import com.fransebastiao.taskmanager.domain.project.ProjectMember;
import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.domain.task.TaskActivity;
import com.fransebastiao.taskmanager.domain.task.TaskCategory;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.request.CreateTaskRequest;
import com.fransebastiao.taskmanager.dto.response.AssigneeAvatarDto;
import com.fransebastiao.taskmanager.dto.response.TaskResponse;
import com.fransebastiao.taskmanager.exception.custom.DuplicateException;
import com.fransebastiao.taskmanager.exception.custom.ResourceNotFoundException;
import com.fransebastiao.taskmanager.repository.ProjectActivityRepository;
import com.fransebastiao.taskmanager.repository.ProjectMemberRepository;
import com.fransebastiao.taskmanager.repository.ProjectRepository;
import com.fransebastiao.taskmanager.repository.TaskActivityRepository;
import com.fransebastiao.taskmanager.repository.TaskCategoryRepository;
import com.fransebastiao.taskmanager.repository.TaskRepository;
import com.fransebastiao.taskmanager.repository.UserRepository;
import com.fransebastiao.taskmanager.service.ProjectMemberService;
import com.fransebastiao.taskmanager.service.TaskService;
import com.fransebastiao.taskmanager.util.AvatarHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {
    private final TaskActivityRepository    taskActivityRepository;
    private final TaskCategoryRepository    taskCategoryRepository;
    private final TaskRepository            taskRepository;
    private final TaskCategoryRepository    categoryRepository;
    private final ProjectRepository         projectRepository;
    private final UserRepository            userRepository;
    private final ProjectActivityRepository projectActivityRepository;
    private final ProjectMemberRepository   memberRepository;
    private final ProjectMemberService      memberService;
    private final AvatarHelper              avatarHelper;

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------
    
    @Transactional
    public TaskResponse criar(CreateTaskRequest request, UUID userId) {
        if (taskRepository.existsByTitleIgnoreCase(request.title())) {
            throw new DuplicateException("Titulo de tarefa já existe");
        }

        User createdBy = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Project project = null;
        if (request.projectId() != null) {
            project = projectRepository.findById(request.projectId()).orElseThrow(() -> new ResourceNotFoundException("Projecto não foi encontrado"));    
            
            if (taskRepository.existsByProjectIdAndTitleIgnoreCase(project.getId(), request.title())) {
                log.error("Erro ao criar tarefa. Titulo de tarefa já existe neste projecto");
                throw new DuplicateException("Titulo de tarefa já existe neste projecto");
            }
        }

        TaskCategory category = null;
        if (request.categoryId() != null) {
            category = taskCategoryRepository.findById(request.categoryId()).orElseThrow(() -> new ResourceNotFoundException("Task category not found"));
        }

        Task task = new Task(request.title(), request.dueDate(), project, category);
        task.setDescription(request.description());
        task.setPriority(request.priority() != null ? request.priority() : Task.Priority.MÉDIA);
        task.setStatus(request.status());
        task.setCreatedBy(createdBy);

        if (request.assigneeIds() != null && !request.assigneeIds().isEmpty()) {
            List<User> assignees = userRepository.findAllById(request.assigneeIds());
            assignees.forEach(task::adicionarAssignee);
        }

        if (request.status() == Task.TaskStatus.COMPLETA) {
            task.complete();
        }

        log.info("Criando tarefa '{}'", task.getTitle());

        Task saved = taskRepository.save(task);

        taskActivityRepository.save(new TaskActivity(
            saved, createdBy,
            "Atividade criada",
            TaskActivity.ActivityType.TASK_CREATED)
        );

        return TaskResponse.from(saved);
    }

    @Transactional
    public TaskResponse criarSubTarefa(UUID parentTaskId, CreateTaskRequest request, UUID userId) {
        Task parentTask = taskRepository.findById(parentTaskId).orElseThrow(() -> new ResourceNotFoundException("Task not found."));

        if (parentTask.isSubtask()) {
            throw new IllegalStateException("It is not possible to create a subtask from a subtask");
        }

        if (taskRepository.existsByParentTaskAndTitleIgnoreCase(parentTask, request.title())) {
            throw new DuplicateException("Subtask with this title already exist for this task");
        }

        User createdBy = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Project project = parentTask.getProject();
        TaskCategory category = request.categoryId() != null
            ? taskCategoryRepository.findById(request.categoryId()).orElseThrow(() -> new ResourceNotFoundException("Task category not found"))
            : parentTask.getCategory();
        
        Task subtask = new Task(request.title(), request.dueDate(), project, category);
        subtask.setDescription(request.description());
        subtask.setPriority(request.priority());
        subtask.setStatus(request.status());
        subtask.setCreatedBy(createdBy);

        if (request.assigneeIds() != null && !request.assigneeIds().isEmpty()) {
            List<User> assignees = userRepository.findAllById(request.assigneeIds());
            assignees.forEach(subtask::adicionarAssignee);
        }

        if (subtask.getStatus() == Task.TaskStatus.COMPLETA) {
            subtask.complete();
        }

        parentTask.addSubtask(subtask);

        log.info("Creating subtask '{}' for task '{}'", subtask.getTitle(), parentTask.getTitle());
        
        taskRepository.save(parentTask);

        taskActivityRepository.save(
            new TaskActivity(
                subtask, createdBy,
                "Subtarefa criada",
                TaskActivity.ActivityType.TASK_CREATED
            )
        );

        return TaskResponse.from(subtask);
    }

    @Transactional
    public TaskResponse updateTask(UUID id, CreateTaskRequest request, UUID userId) {
        if (taskRepository.existsByTitleIgnoreCaseAndIdNot(request.title(), id)) {
            throw new DuplicateException("Titulo de tarefa já existe");
        }

        Task task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tarefa não foi encontrada"));
        User createdBy = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        final Project project;
        if (request.projectId() != null) {
            project = projectRepository.findById(request.projectId()).orElseThrow(() -> new ResourceNotFoundException("Projecto não foi encontrado"));
            // ✅ Duplicate validation (excluding current task)
            if (taskRepository.existsByProjectIdAndTitleIgnoreCaseAndIdNot(
                    project.getId(),
                    request.title(),
                    id
            )) {
                throw new DuplicateException("Titulo de tarefa já existe neste projecto");
            }
        } else {
            project = null;
        }

        task.setTitle(request.title());
        task.setDueDate(request.dueDate());
        task.setProject(project);
        task.setDescription(request.description());
        task.setPriority(request.priority() != null ? request.priority() : Task.Priority.MÉDIA);
        task.setStatus(request.status());
        task.setCreatedBy(createdBy);

        if (request.categoryId() != null) {
            TaskCategory category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task category not found"));
            task.setCategory(category);
        }

        if (request.status() == Task.TaskStatus.COMPLETA) {
            task.complete();
        }

        // ⚠️ Replace assignees safely (not just add)
        if (request.assigneeIds() != null) {

            List<User> assignees = userRepository.findAllById(request.assigneeIds());

            task.getAssignees().clear();

            assignees.forEach(user -> {
                memberService.ensureMember(project, user, "TRABALHADOR");
                task.adicionarAssignee(user);
            });
        }

        log.info("Atualizando tarefa '{}'", task.getTitle());

        return TaskResponse.from(task);
    }

    public String generateUniqueName(String baseName) {
        int counter = 1;
        String newName = baseName;

        while (taskRepository.existsByTitleIgnoreCase(newName)) {
            newName = baseName + " (" + counter + ")";
            counter++;
        }

        return newName;
    }

    @Transactional(readOnly = true)
    public TaskResponse buscarPorId(UUID id) {
        return taskRepository.findByIdWithAssignees(id)
            .map(TaskResponse::from)    
            .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada com id: " + id));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listarPorProjecto(UUID projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listarPorAssignee(UUID userId) {
        return taskRepository.findByAssigneeId(userId).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listarPorProjectoEStatus(UUID projectId, Task.TaskStatus status) {
        return taskRepository.findByProjectIdAndStatus(projectId, status).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listarEmAtraso() {
        return taskRepository.findOverdueTasks(LocalDate.now()).stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse actualizarStatus(UUID id, Task.TaskStatus novoStatus, UUID userId) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tarefa não foi encontrada"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuario não foi encontrado"));
        task.setStatus(novoStatus);
        log.info("Tarefa {} status atualizado para {}", id, novoStatus);

        Task saved = taskRepository.save(task);

        taskActivityRepository.save(new TaskActivity(
            saved, user,
            "Úsuario \"" + user.getName() + "Atualizou o estado da Tarefa \"" + saved.getTitle() + "Para \"" + novoStatus,
            TaskActivity.ActivityType.STATUS_CHANGED)
        );
        return TaskResponse.from(saved);
    }

    public TaskResponse concluir(UUID id, UUID userId) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tarefa não foi encontrada"));
        task.complete();
        log.info("Tarefa {} completa", id);

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Task saved = taskRepository.save(task);

        taskActivityRepository.save(new TaskActivity(
            saved, user,
            "Tarefa \"" + saved.getTitle() + "\" marcada como concluída",
            TaskActivity.ActivityType.TASK_COMPLETED)
        );

        if (saved.getProject() != null) {
            projectActivityRepository.save(new ProjectActivity(
                saved.getProject(), user,
                "Tarefa \"" + saved.getTitle() + "\" marcada como concluída",
                ProjectActivity.ActivityType.TASK_COMPLETED)
            );
        }

        return TaskResponse.from(saved);
    }

    public void eliminar(UUID id) {
        taskRepository.delete(taskRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tarefa não foi encontrada")));
        log.info("Trefa apagada: {}", id);
    }

    // -------------------------------------------------------------------------
    // Gestão de assignees
    // -------------------------------------------------------------------------

    @Transactional
    public TaskResponse adicionarAssignee(UUID taskId, UUID userId) {

        Task task = taskRepository.findByIdWithAssignees(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não foi encontrada"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario não foi encontrado"));

        if (task.temAssignee(user)) {
            throw new IllegalArgumentException("Usuario já foi delegado para esta tarefa");
        }

        if (task.getProject() != null) {
            memberService.ensureMember(task.getProject(), user, "TRABALHADOR");
        }

        task.adicionarAssignee(user);

        taskActivityRepository.save(new TaskActivity(
            task, user,
            "Membro adicionado à \"" + "Tarefa \"" + task.getTitle(),
            TaskActivity.ActivityType.ASSIGNEE_ADDED)
        );

        log.info("Usuario {} adicionado à tarefa {}", userId, taskId);

        return TaskResponse.from(task);
    }

    @Transactional
    public TaskResponse adicionarAssignees(UUID taskId, List<UUID> userIds) {

        Task task = taskRepository.findByIdWithAssignees(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não foi encontrada"));

        List<User> users = userRepository.findAllById(userIds);

        if (users.size() != userIds.size()) {
            throw new ResourceNotFoundException("Um ou mais usuarios não foram encontrados");
        }

        users.stream()
            .filter(u -> !task.temAssignee(u))
            .forEach(user -> {
                if (task.getProject() != null) {
                    memberService.ensureMember(task.getProject(), user, "TRABALHADOR");
                }
                task.adicionarAssignee(user);
                taskActivityRepository.save(new TaskActivity(
                    task, user,
                    "Membro adicionado à \"" + "Tarefa \"" + task.getTitle(),
                    TaskActivity.ActivityType.ASSIGNEE_ADDED
                )
            );
        });

        log.info("Adicionados {} delegados para tarefa {}", users.size(), taskId);

        return TaskResponse.from(task);
    }

    @Transactional
    public void removerMembro(UUID taskId, UUID userId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        boolean removed = task.getAssignees().removeIf(user -> {
            taskActivityRepository.save(new TaskActivity(
                task, user,
                "Membro removido da \"" + "Tarefa \"" + task.getTitle(),
                TaskActivity.ActivityType.ASSIGNEE_REMOVED)
            );
            return user.getId().equals(userId);
        });

        if (!removed) {
            throw new ResourceNotFoundException("User not assigned to this task");
        }

        Project project = task.getProject();

        if (project == null) {
            log.info("User {} removed from standalone task {}", userId, taskId);
            return;
        }

        long taskCount = taskRepository.countByProjectId(project.getId());

        if (taskCount == 1) {
            memberRepository.deleteByProjectIdAndUserId(project.getId(), userId);

            log.info("User {} removed from project {} (last task case)", userId, project.getId());
        } else {
            log.info("User {} removed only from task {} (project has multiple tasks)", userId, taskId);
        }
        
        task.getAssignees().removeIf(user -> user.getId().equals(userId));
    }


    public List<AssigneeAvatarDto> getAvailableMembers(UUID taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        Set<UUID> alreadyAssigned = task.getAssignees().stream().map(User::getId).collect(Collectors.toSet());
        
        // Task belongs to a project — return project members not yet assigned
        if (task.getProject() != null) {
        return memberRepository.findByProjectId(task.getProject().getId()).stream()
            .map(ProjectMember::getUser)
            .filter(user -> !alreadyAssigned.contains(user.getId()))
            .map(avatarHelper::toAssigneeAvatar)
            .toList();
        }

        // Task has no project — return all users in the system not yet assigned
        return userRepository.findAll().stream()
            .filter(user -> !alreadyAssigned.contains(user.getId()))
            .map(avatarHelper::toAssigneeAvatar)
            .toList();
    }
}