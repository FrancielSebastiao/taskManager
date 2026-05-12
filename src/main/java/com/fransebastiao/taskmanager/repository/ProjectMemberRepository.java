package com.fransebastiao.taskmanager.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.project.ProjectMember;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    Page<ProjectMember> findByProjectId(UUID projectId, Pageable pageable);

    @Query("SELECT m FROM ProjectMember m JOIN FETCH m.user WHERE m.project.id = :projectId")
    List<ProjectMember> findByProjectId(@Param("projectId") UUID projectId);

    Page<ProjectMember> findByUserId(UUID userId, Pageable pageable);

    List<ProjectMember> findByUserId(UUID userId);

    Optional<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);

    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);

    @Modifying
    @Query("DELETE FROM ProjectMember m WHERE m.project.id = :projectId AND m.user.id = :userId")
    void deleteByProjectIdAndUserId(
            @Param("projectId") UUID projectId,
            @Param("userId") UUID userId);

    @Query("SELECT COUNT(DISTINCT m.user) FROM ProjectMember m")
    long countDistinctUsers();
    @Query("""
        SELECT COUNT(DISTINCT m.user) FROM ProjectMember m
        WHERE m.project.status = 'EM_PROGRESSO'
    """)
    long countUsersInActiveProjects();

    @Query("""
        SELECT COUNT(DISTINCT m2.user.id)
        FROM ProjectMember m1
        JOIN ProjectMember m2 ON m1.project.id = m2.project.id
        WHERE m1.user.id = :userId     
    """)
    long countTeamMembers(@Param("userId") UUID userId);

    @Query("""
        SELECT COUNT(DISTINCT m2.user.id)
        FROM ProjectMember m1
        JOIN ProjectMember m2 ON m1.project.id = m2.project.id
        JOIN m2.project p
        WHERE m1.user.id = :userId 
        AND p.status = 'EM_PROGRESSO'    
    """)
    long countActiveTeamMembers(@Param("userId") UUID userId);

    @Query("""
        SELECT m FROM ProjectMember m
        LEFT JOIN FETCH m.user
        WHERE m.project.id = :projectId
    """)
    Page<ProjectMember> findTeamByProjectId(@Param("projectId") UUID projectId, Pageable pageable);
}
