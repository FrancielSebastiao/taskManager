package com.fransebastiao.taskmanager.service;

import java.util.List;
import java.util.UUID;

import com.fransebastiao.taskmanager.dto.request.CreateCategoryRequest;
import com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse;

public interface CategoryService {
    NameAndDescriptionResponse createProjectCategory(CreateCategoryRequest request);
    NameAndDescriptionResponse updateProjectCategory(UUID id, CreateCategoryRequest request);
    List<NameAndDescriptionResponse> getProjectCategories();
    void deleteProjectCategory(UUID id);
    NameAndDescriptionResponse createTaskCategory(CreateCategoryRequest request);
    NameAndDescriptionResponse updateTaskCategory(UUID id, CreateCategoryRequest request);
    List<NameAndDescriptionResponse> getTaskCategories();
    void deleteTaskCategory(UUID id);
}
