package com.fransebastiao.taskmanager.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fransebastiao.taskmanager.domain.attachment.TaskPhoto;
import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.domain.task.TaskActivity;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.response.TaskPhotoResponse;
import com.fransebastiao.taskmanager.exception.custom.InvalidFileException;
import com.fransebastiao.taskmanager.repository.TaskActivityRepository;
import com.fransebastiao.taskmanager.repository.TaskPhotoRepository;
import com.fransebastiao.taskmanager.repository.TaskRepository;
import com.fransebastiao.taskmanager.repository.UserRepository;
import com.fransebastiao.taskmanager.service.S3Service;
import com.fransebastiao.taskmanager.service.TaskPhotoService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskPhotoServiceImpl implements TaskPhotoService {
    private final TaskActivityRepository    taskActivityRepository;
    private final TaskPhotoRepository       photoRepository;
    private final TaskRepository            taskRepository;
    private final UserRepository            userRepository;
    private final S3Service                 s3Service;

    @Value("${app.upload.allowed-types}")
    private List<String> allowedTypes;

    @Transactional
    public List<TaskPhotoResponse> uploadMultiple(
            UUID taskId,
            UUID uploadedById,
            List<MultipartFile> files,
            List<String> captions
    ) throws IOException {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        User uploadedBy = userRepository.findById(uploadedById)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<TaskPhoto> photos = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {

            MultipartFile file = files.get(i);
            validarFicheiro(file);

            String extension = extrairExtensao(file.getOriginalFilename());

            String s3Key = buildS3Key(
                    taskId,
                    UUID.randomUUID() + "." + extension
            );

            // ✅ Use stream instead of bytes (better for performance)
            s3Service.upload(s3Key, file.getInputStream(), file.getSize(), file.getContentType());

            TaskPhoto photo = new TaskPhoto(
                    task,
                    uploadedBy,
                    s3Key,
                    extension,
                    file.getSize()
            );

            // ✅ Handle optional captions safely
            if (captions != null && captions.size() > i) {
                photo.setCaption(captions.get(i));
            }

            photos.add(photo);

            taskActivityRepository.save(new TaskActivity(
                task, uploadedBy,
                "Nova imagem \"" + file.getOriginalFilename() + "\" carregada",
                TaskActivity.ActivityType.PHOTO_UPLOADED)
            );
        }

        List<TaskPhoto> savedPhotos = photoRepository.saveAll(photos);

        log.info("Uploaded {} photos for task {}", savedPhotos.size(), taskId);

        // ✅ Convert to DTO here (BEST PRACTICE)
        return savedPhotos.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskPhotoResponse> listarPorTask(UUID taskId) {
        return photoRepository.findByTaskId(taskId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskPhotoResponse> listarPorProjecto(UUID projectId) {
        return photoRepository.findByProjectId(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskPhotoResponse buscarPorId(UUID id) {
        TaskPhoto photo = photoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found: " + id));
        return toResponse(photo);
    }

    @Transactional
    public void eliminar(UUID id) {
        TaskPhoto photo = photoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found: " + id));

        s3Service.delete(photo.getS3Key()); // remove do S3
        photoRepository.delete(photo);      // remove da DB
        log.info("Photo deleted: {}", id);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private void validarFicheiro(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }
        if (!allowedTypes.contains(file.getContentType())) {
            throw new InvalidFileException(
                "File type not allowed: " + file.getContentType() +
                ". Allowed: " + allowedTypes);
        }
    }

    private String extrairExtensao(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new InvalidFileException("Invalid filename: " + filename);
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String buildS3Key(UUID taskId, String extension) {
        return String.format("tasks/%s/photos/%s.%s",
                taskId, UUID.randomUUID(), extension);
    }

   public TaskPhotoResponse toResponse(TaskPhoto p) {

        String url = s3Service.gerarUrlPresignada(p.getS3Key());

        return new TaskPhotoResponse(
                p.getId(),
                p.getTask().getId(),
                p.getUploadedBy().getName(),
                url,
                p.getExtension(),
                p.getFileSizeBytes(),
                p.getCaption(),
                p.getUploadedAt()
        );
    }
}
