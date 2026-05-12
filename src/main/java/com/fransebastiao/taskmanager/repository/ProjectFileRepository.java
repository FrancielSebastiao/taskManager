package com.fransebastiao.taskmanager.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.project.ProjectFile;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, UUID> {
    Page<ProjectFile> findByProjectIdOrderByUploadedAtDesc(UUID projectId, Pageable pageable);
    void deleteByProjectId(UUID projectId);
}
