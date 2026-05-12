package com.fransebastiao.taskmanager.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.dto.response.AssigneeDetailDto;
import com.fransebastiao.taskmanager.dto.response.PagedResponse;
import com.fransebastiao.taskmanager.dto.response.TaskActivityDto;
import com.fransebastiao.taskmanager.dto.response.TaskCommentDetailDto;
import com.fransebastiao.taskmanager.dto.response.TaskDetailResponse;
import com.fransebastiao.taskmanager.dto.response.TaskFileDto;
import com.fransebastiao.taskmanager.dto.response.TaskImageDto;
import com.fransebastiao.taskmanager.service.TaskDetailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskDetailController {
    private final TaskDetailService taskDetailService;

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAuthority('LER_TAREFAS')")
    public ResponseEntity<TaskDetailResponse> getDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(taskDetailService.getDetail(id));
    }

    @GetMapping("/{id}/detail/assignees")
    public ResponseEntity<PagedResponse<AssigneeDetailDto>> getTaskAssignees(
        @PathVariable UUID id,
        @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(taskDetailService.getTaskAssigness(id, page));
    }

    @GetMapping("/{id}/detail/images")
    public ResponseEntity<PagedResponse<TaskImageDto>> getTaskImages(
        @PathVariable UUID id,
        @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(taskDetailService.getTaskImages(id, page));
    }

    @GetMapping("/{id}/detail/files")
    public ResponseEntity<PagedResponse<TaskFileDto>> getTaskFiles(
        @PathVariable UUID id,
        @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(taskDetailService.getTaskFiles(id, page));
    }

    @GetMapping("/{id}/detail/comments")
    public ResponseEntity<PagedResponse<TaskCommentDetailDto>> getTaskComments(
        @PathVariable UUID id,
        @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(taskDetailService.getTaskComments(id, page));
    }

    @GetMapping("/{id}/detail/activities")
    public ResponseEntity<PagedResponse<TaskActivityDto>> getTaskActivities(
        @PathVariable UUID id,
        @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(taskDetailService.getTaskActivities(id, page));
    }
}
