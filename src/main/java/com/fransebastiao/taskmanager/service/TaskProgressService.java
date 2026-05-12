package com.fransebastiao.taskmanager.service;

import java.util.List;
import java.util.UUID;

import com.fransebastiao.taskmanager.dto.request.UpdateProgressRequest;
import com.fransebastiao.taskmanager.dto.response.TaskProgressResponse;

public interface TaskProgressService {
    TaskProgressResponse actualizarProgresso(UUID taskId, UUID userId, UpdateProgressRequest request);
    TaskProgressResponse buscarProgresso(UUID taskId);
    List<TaskProgressResponse> listarProgressoPorProjecto(UUID projectId);
}
