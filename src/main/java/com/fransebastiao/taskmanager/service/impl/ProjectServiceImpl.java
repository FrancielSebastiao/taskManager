package com.fransebastiao.taskmanager.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.domain.project.ProjectActivity;
import com.fransebastiao.taskmanager.domain.project.ProjectCategory;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.request.CreateProjectRequest;
import com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectResponse;
import com.fransebastiao.taskmanager.exception.custom.DuplicateException;
import com.fransebastiao.taskmanager.exception.custom.ResourceNotFoundException;
import com.fransebastiao.taskmanager.repository.ProjectActivityRepository;
import com.fransebastiao.taskmanager.repository.ProjectCategoryRepository;
import com.fransebastiao.taskmanager.repository.ProjectRepository;
import com.fransebastiao.taskmanager.repository.UserRepository;
import com.fransebastiao.taskmanager.service.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProjectServiceImpl implements ProjectService {
    private final ProjectActivityRepository projectActivityRepository;
    private final ProjectCategoryRepository projectCategoryRepository;
    private final ProjectRepository    projectRepository;
    private final UserRepository       userRepository;

    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        if (projectRepository.existsByNameIgnoreCase(request.name())) {
            log.error("Erro ao criar projecto. Nome do projecto já existe");
            throw new DuplicateException("Nome de projecto já existe");
        }

        User manager = userRepository.findById(request.managerId())
                .orElseThrow(() -> new ResourceNotFoundException("Gestor não foi encontrado"));

        ProjectCategory category = projectCategoryRepository.findById(request.categoryId()).orElseThrow(() -> new ResourceNotFoundException("Categoria de projecto não foi encontrada"));

        Project project = new Project(
                request.name(),
                request.startDate(),
                request.deadline(),
                manager,
                category
        );
        project.setDescription(request.description());
        project.setStatus(request.status());
        project.setBudget(request.budget());

        log.info("Criando projecto: {}", project.getName());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse update(UUID id, CreateProjectRequest request) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Projecto não foi encontrado"));

        if (projectRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            log.error("Erro ao atualizar projecto. Nome do projecto já existe");
            throw new DuplicateException("Nome de projecto já existe");
        }

        User manager = userRepository.findById(request.managerId())
                .orElseThrow(() -> new EntityNotFoundException("Manager not found"));

        ProjectCategory category = projectCategoryRepository.findById(request.categoryId()).orElseThrow(() -> new ResourceNotFoundException("Categoria de projecto não foi encontrada"));

        project.setName(request.name());
        project.setStartDate(request.startDate());
        project.setDeadline(request.deadline());
        project.setManager(manager);
        project.setCategory(category);
        project.setDescription(request.description());
        project.setBudget(request.budget());

        log.info("Atualizando projecto: {}", project.getName());
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<NameAndDescriptionResponse> getProjectNameAndDescription() {
        return projectRepository.findProjectNames();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(UUID id) {
        return projectRepository.findById(id)
            .map(ProjectResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("Projecto não foi encontrado: " + id));
    }

    @Transactional
    public ProjectResponse updateStatus(UUID id, Project.ProjectStatus novoStatus, UUID userId) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Projecto não foi encontrado"));
        project.setStatus(novoStatus);
        log.info("Projecto {} status atualizado para {}", id, novoStatus);

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        projectActivityRepository.save(new ProjectActivity(
            project, user,
            "Estado alterado para " + novoStatus.name(),
            ProjectActivity.ActivityType.STATUS_CHANGED)
        );

        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateOrcamento(UUID id, BigDecimal novoOrcamento) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Projecto não foi encontrado"));
        project.setBudget(novoOrcamento);
        return ProjectResponse.from(project);
    }

    @Transactional
    public void delete(UUID id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Projecto não foi encontrado"));
        projectRepository.delete(project);
        log.info("Projecto removido: {}", id);
    }
}
