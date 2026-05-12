package com.fransebastiao.taskmanager.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.project.ProjectCategory;
import com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse;


@Repository
public interface ProjectCategoryRepository extends JpaRepository<ProjectCategory, UUID> {
    
    Optional<ProjectCategory> findById(UUID id);

    Optional<ProjectCategory> findByName(String name);

    List<ProjectCategory> findByActiveTrue();

    boolean existsByName(String name);

    @Query("""
        SELECT new com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse(
            c.id,
            c.name,
            c.description
        )      
        FROM ProjectCategory c  
    """)
    List<NameAndDescriptionResponse> findProjectCategories();
}
