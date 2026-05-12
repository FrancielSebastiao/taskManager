package com.fransebastiao.taskmanager.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.attachment.TaskPhoto;

@Repository
public interface TaskPhotoRepository extends JpaRepository<TaskPhoto, UUID> {

    List<TaskPhoto> findByTaskId(UUID taskId);

    @Query("SELECT p FROM TaskPhoto p WHERE p.task.project.id = :projectId")
    List<TaskPhoto> findByProjectId(@Param("projectId") UUID projectId);

    void deleteByTaskId(UUID taskId);

    @Query("""
        SELECT tp 
        FROM TaskPhoto tp
        WHERE tp.task.id = :taskId    
    """)
    Page<TaskPhoto> findTaskPhotos(@Param("taskId") UUID taskId, Pageable pageable);
}
