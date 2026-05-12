package com.fransebastiao.taskmanager.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fransebastiao.taskmanager.dto.response.TaskPhotoResponse;
import com.fransebastiao.taskmanager.service.TaskPhotoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskPhotoController {

    private final TaskPhotoService photoService;

    @PostMapping(value = "{taskId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('CARREGAMENTO_DE_IMAGENS')")
    public ResponseEntity<List<TaskPhotoResponse>> upload(
            @PathVariable UUID taskId,
            @RequestParam UUID uploadedById,
            @RequestParam List<MultipartFile> files,
            @RequestParam(required = false) List<String> captions
    ) throws IOException {

        List<TaskPhotoResponse> response = photoService.uploadMultiple(
                taskId,
                uploadedById,
                files,
                captions
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}/photos")
    public ResponseEntity<List<TaskPhotoResponse>> listarPorTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(photoService.listarPorTask(taskId));
    }

    @GetMapping("/{taskId}/photos/{photoId}")
    public ResponseEntity<TaskPhotoResponse> buscarPorId(
            @PathVariable UUID taskId,
            @PathVariable UUID photoId) {
        return ResponseEntity.ok(photoService.buscarPorId(photoId));
    }

    @DeleteMapping("/{taskId}/photos/{photoId}")
    @PreAuthorize("hasAuthority('APAGAR_IMAGENS')")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID taskId,
            @PathVariable UUID photoId) {
        photoService.eliminar(photoId);
        return ResponseEntity.noContent().build();
    }
}
