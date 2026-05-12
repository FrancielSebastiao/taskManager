package com.fransebastiao.taskmanager.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.task.TaskFile;

@Repository
public interface TaskFileRepository extends JpaRepository<TaskFile, UUID> {
    Page<TaskFile> findByTaskIdOrderByUploadedAtDesc(UUID taskId, Pageable pageable);
    void deleteByTaskId(UUID taskId);
}
