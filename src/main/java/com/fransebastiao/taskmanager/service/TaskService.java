package com.fransebastiao.taskmanager.service;

import java.util.List;
import java.util.UUID;


import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.dto.request.CreateTaskRequest;
import com.fransebastiao.taskmanager.dto.response.AssigneeAvatarDto;
import com.fransebastiao.taskmanager.dto.response.TaskResponse;

public interface TaskService {
    TaskResponse criar(CreateTaskRequest request, UUID userId);
    TaskResponse criarSubTarefa(UUID parentTaskId, CreateTaskRequest request, UUID userId);
    TaskResponse updateTask(UUID id, CreateTaskRequest request, UUID userId);
    TaskResponse buscarPorId(UUID id);
    List<TaskResponse> listarPorProjecto(UUID projectId);
    List<TaskResponse> listarPorAssignee(UUID userId);
    List<TaskResponse> listarPorProjectoEStatus(UUID projectId, Task.TaskStatus status);
    List<TaskResponse> listarEmAtraso();
    TaskResponse actualizarStatus(UUID id, Task.TaskStatus novoStatus, UUID userId);
    TaskResponse concluir(UUID id, UUID userId);
    void eliminar(UUID id);
    
    TaskResponse adicionarAssignee(UUID taskId, UUID userId);
    TaskResponse adicionarAssignees(UUID taskId, List<UUID> userIds);
    void removerMembro(UUID taskId, UUID userId);
    List<AssigneeAvatarDto> getAvailableMembers(UUID taskId);
}
