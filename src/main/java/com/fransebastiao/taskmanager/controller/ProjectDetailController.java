package com.fransebastiao.taskmanager.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.dto.response.ActivityDto;
import com.fransebastiao.taskmanager.dto.response.PagedResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectDetailResponse;
import com.fransebastiao.taskmanager.dto.response.ProjectFileDto;
import com.fransebastiao.taskmanager.dto.response.TaskSummaryDto;
import com.fransebastiao.taskmanager.dto.response.TeamMemberDetailDto;
import com.fransebastiao.taskmanager.service.ProjectDetailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectDetailController {
    private final ProjectDetailService detailService;
    // Detalhe completo com primeira página de cada secção
    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAuthority('LER_PROJECTOS')")
    public ResponseEntity<ProjectDetailResponse> getDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(detailService.getDetail(id));
    }
    // "Ver mais" — paginação independente por secção
    @GetMapping("/{id}/detail/tasks")
    @PreAuthorize("hasAuthority('LER_TAREFAS')")
    public ResponseEntity<PagedResponse<TaskSummaryDto>> getTasks(
        @PathVariable UUID id,
        @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(detailService.getTasks(id, page));
    }
    @GetMapping("/{id}/detail/activities")
    @PreAuthorize("hasAuthority('LER_PROJECTOS')")
    public ResponseEntity<PagedResponse<ActivityDto>> getActivities(
        @PathVariable UUID id,
        @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(detailService.getActivities(id, page));
    }
    @GetMapping("/{id}/detail/team")
    @PreAuthorize("hasAuthority('LER_PROJECTOS')")
    public ResponseEntity<PagedResponse<TeamMemberDetailDto>> getTeam(
        @PathVariable UUID id,
        @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(detailService.getTeam(id, page));
    }
    @GetMapping("/{id}/detail/files")
    @PreAuthorize("hasAuthority('LER_PROJECTOS')")
    public ResponseEntity<PagedResponse<ProjectFileDto>> getFiles(
        @PathVariable UUID id,
        @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(detailService.getFiles(id, page));
    }
}