package com.fransebastiao.taskmanager.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.project.ProjectCategory;
import com.fransebastiao.taskmanager.domain.task.TaskCategory;
import com.fransebastiao.taskmanager.dto.request.CreateCategoryRequest;
import com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse;
import com.fransebastiao.taskmanager.exception.custom.DuplicateException;
import com.fransebastiao.taskmanager.exception.custom.ResourceNotFoundException;
import com.fransebastiao.taskmanager.mapper.NameAndDescriptionMapper;
import com.fransebastiao.taskmanager.repository.ProjectCategoryRepository;
import com.fransebastiao.taskmanager.repository.TaskCategoryRepository;
import com.fransebastiao.taskmanager.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final ProjectCategoryRepository projectCategoryRepository;
    private final TaskCategoryRepository taskCategoryRepository;

    @Transactional
    @Override
    public NameAndDescriptionResponse createProjectCategory(CreateCategoryRequest request) {
        ProjectCategory newCategory = new ProjectCategory(request.name(), request.description());
        ProjectCategory saved = projectCategoryRepository.save(newCategory);
        log.info("Categoria de projecto criada: {}", saved.getName());
        return NameAndDescriptionMapper.mapToProjectCategory(saved); 
    }

    @Transactional
    @Override
    public NameAndDescriptionResponse updateProjectCategory(UUID id, CreateCategoryRequest request) {
        ProjectCategory category = projectCategoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Categoria de projecto não foi encontrada"));
        if (projectCategoryRepository.existsByName(request.name()))  {
            log.error("Erro ao atualizar categoria de projecto. Nome da categoria já existe" + request.name());
            throw new DuplicateException("Nome da categoria de projecto já existe");
        }
        category.setName(request.name());
        category.setDescription(request.description());
        return NameAndDescriptionMapper.mapToProjectCategory(projectCategoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    @Override
    public List<NameAndDescriptionResponse> getProjectCategories() {
        return projectCategoryRepository.findProjectCategories();
    }

    @Transactional
    @Override
    public void deleteProjectCategory(UUID id) {
        ProjectCategory category = projectCategoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Categoria de projecto não foi encontrada"));
        projectCategoryRepository.delete(category);
    }

    @Transactional
    @Override
    public NameAndDescriptionResponse createTaskCategory(CreateCategoryRequest request) {
        TaskCategory newCategory = new TaskCategory(request.name(), request.description());
        TaskCategory saved = taskCategoryRepository.save(newCategory);
        log.info("Categoria de tarefa criada: {}", saved.getName());

        return NameAndDescriptionMapper.mapToTaskCategory(saved);
    }

    @Transactional
    @Override
    public NameAndDescriptionResponse updateTaskCategory(UUID id, CreateCategoryRequest request) {
        TaskCategory category = taskCategoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Categoria de tarefa não foi encontrada"));
        if (taskCategoryRepository.existsByName(request.name()))  {
            log.error("Erro ao atualizar categoria de tarefa. Nome da categoria já existe" + request.name());
            throw new DuplicateException("Nome da categoria de tarefa já existe");
        }
        category.setName(request.name());
        category.setDescription(request.description());
        return NameAndDescriptionMapper.mapToTaskCategory(taskCategoryRepository.save(category));
    }

    @Transactional(readOnly =  true)
    @Override
    public List<NameAndDescriptionResponse> getTaskCategories() {
        return taskCategoryRepository.findTaskCategories();
    }

    @Transactional
    @Override
    public void deleteTaskCategory(UUID id) {
        TaskCategory category = taskCategoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Categoria de tarefa não foi encontrada"));
        taskCategoryRepository.delete(category);
    }
}
