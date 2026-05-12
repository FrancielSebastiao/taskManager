package com.fransebastiao.taskmanager.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.fransebastiao.taskmanager.domain.attachment.TaskPhoto;
import com.fransebastiao.taskmanager.dto.response.TaskPhotoResponse;

public interface TaskPhotoService {
    List<TaskPhotoResponse> uploadMultiple(
            UUID taskId,
            UUID uploadedById,
            List<MultipartFile> files,
            List<String> captions
    ) throws IOException;
    List<TaskPhotoResponse> listarPorTask(UUID taskId);
    List<TaskPhotoResponse> listarPorProjecto(UUID projectId);
    TaskPhotoResponse buscarPorId(UUID id);
    void eliminar(UUID id);

    TaskPhotoResponse toResponse(TaskPhoto p);
}
