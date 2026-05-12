package com.fransebastiao.taskmanager.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.task.TaskActivity;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivity, UUID> {
    Page<TaskActivity> findByTaskIdOrderByCreatedAtDesc(UUID taskId, Pageable pageable);
}
