package com.fransebastiao.taskmanager.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.dto.request.CreateCategoryRequest;
import com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse;
import com.fransebastiao.taskmanager.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/project/category")
    public ResponseEntity<NameAndDescriptionResponse> createProjectCategory(@RequestBody @Valid CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createProjectCategory(request));
    }

    @PutMapping("/project/category/{id}")
    public ResponseEntity<NameAndDescriptionResponse> updateProjectCategory(@PathVariable UUID id, @RequestBody @Valid CreateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateProjectCategory(id, request));
    }

    @GetMapping("/project/categories")
    public ResponseEntity<List<NameAndDescriptionResponse>> getProjectCategories() {
        return ResponseEntity.ok(categoryService.getProjectCategories());
    }

    @DeleteMapping("/project/category/{id}")
    public ResponseEntity<Void> deleteProjectCategory(@PathVariable UUID id) {
        categoryService.deleteProjectCategory(id);
        return ResponseEntity.noContent().build();
    } 

    @PostMapping("/task/category")
    public ResponseEntity<NameAndDescriptionResponse> createTaskCategory(@RequestBody @Valid CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createTaskCategory(request));
    }

    @PutMapping("/task/category/{id}")
    public ResponseEntity<NameAndDescriptionResponse> updateTaskCategory(@PathVariable UUID id, @RequestBody @Valid CreateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateTaskCategory(id, request));
    }

    @GetMapping("/task/categories")
    public ResponseEntity<List<NameAndDescriptionResponse>> getTaskCategories() {
        return ResponseEntity.ok(categoryService.getTaskCategories());
    }

    @DeleteMapping("/task/category/{id}")
    public ResponseEntity<List<NameAndDescriptionResponse>> deleteTaskCategory(@PathVariable UUID id) {
        categoryService.deleteTaskCategory(id);
        return ResponseEntity.noContent().build();
    }
}
