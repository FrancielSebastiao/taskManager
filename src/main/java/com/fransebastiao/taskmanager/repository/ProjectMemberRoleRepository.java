package com.fransebastiao.taskmanager.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.project.ProjectMemberRole;
import com.fransebastiao.taskmanager.dto.response.NameResponse;

@Repository
public interface ProjectMemberRoleRepository extends JpaRepository<ProjectMemberRole, UUID> {
    Optional<ProjectMemberRole> findById(UUID id);
    Optional<ProjectMemberRole> findByName(String name);
    @Query("""
        SELECT new com.fransebastiao.taskmanager.dto.response.NameResponse(
            r.id,
            r.name
        )
        FROM ProjectMemberRole r
        """)
    Page<NameResponse> findAllRolesPage(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
}
