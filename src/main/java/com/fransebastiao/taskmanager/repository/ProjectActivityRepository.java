package com.fransebastiao.taskmanager.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.project.ProjectActivity;

@Repository
public interface ProjectActivityRepository extends JpaRepository<ProjectActivity, UUID> {
    Page<ProjectActivity> findByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);
}
