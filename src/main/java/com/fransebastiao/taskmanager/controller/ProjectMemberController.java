package com.fransebastiao.taskmanager.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.dto.response.ProjectMemberResponse;
import com.fransebastiao.taskmanager.service.ProjectMemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Validated
public class ProjectMemberController {

    private final ProjectMemberService memberService;

    @PostMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectMemberResponse> adicionarMembro(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @RequestParam String role) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(memberService.adicionarMembro(projectId, userId, role));
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<Page<ProjectMemberResponse>> listarMembros(
        @PathVariable UUID projectId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(memberService.listarMembrosPorProjecto(projectId, pageable));
    }

    @GetMapping("/members/user/{userId}")
    public ResponseEntity<Page<ProjectMemberResponse>> listarProjectosPorUser(
        @PathVariable UUID userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(memberService.listarProjectosPorUser(userId, pageable));
    }

    @PatchMapping("/{projectId}/members/{userId}/role")
    public ResponseEntity<ProjectMemberResponse> actualizarRole(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @RequestParam String role) {
        return ResponseEntity.ok(memberService.actualizarRole(projectId, userId, role));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<Void> removerMembro(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        memberService.removerMembro(projectId, userId);
        return ResponseEntity.noContent().build();
    }
}
