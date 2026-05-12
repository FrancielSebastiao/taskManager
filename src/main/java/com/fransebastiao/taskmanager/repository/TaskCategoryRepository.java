package com.fransebastiao.taskmanager.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.task.TaskCategory;
import com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse;

@Repository
public interface TaskCategoryRepository extends JpaRepository<TaskCategory, UUID> {

    Optional<TaskCategory> findById(UUID id);

    Optional<TaskCategory> findByName(String name);

    List<TaskCategory> findByActiveTrue();

    boolean existsByName(String name);

    @Query("""
        SELECT new com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse(
            c.id,
            c.name,
            c.description
        )      
        FROM TaskCategory c  
    """)
    List<NameAndDescriptionResponse> findTaskCategories();
}