package com.fransebastiao.taskmanager.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.domain.project.Project.ProjectStatus;
import com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

    List<Project> findByStatus(Project.ProjectStatus status);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.team WHERE p.id = :id")
    Optional<Project> findByIdWithTeam(@Param("id") UUID id);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.materials WHERE p.id = :id")
    Optional<Project> findByIdWithMaterials(@Param("id") UUID id);

    @Query("""
        SELECT p FROM Project p
        LEFT JOIN FETCH p.tasks t
        WHERE p.id = :id
        """)
    Optional<Project> findByIdWithTasks(@Param("id") UUID id);

    @Query("""
        SELECT p FROM Project p
        WHERE p.deadline < :date
        AND p.status NOT IN ('COMPLETED', 'CANCELLED')
        """)
    List<Project> findOverdueProjects(@Param("date") LocalDate date);

    @Query("""
        SELECT DISTINCT p FROM Project p
        LEFT JOIN FETCH p.team t
        LEFT JOIN FETCH t.user
        LEFT JOIN FETCH p.tasks
        WHERE p.id = :id
        """)
    Optional<Project> findByIdWithTeamAndTasks(@Param("id") UUID id);

    // Método auxiliar para buscar projetos com joins otimizados
    @EntityGraph(attributePaths = {"manager", "category", "team.user", "tasks"})
    @Query("SELECT DISTINCT p FROM Project p")
    List<Project> findAllWithDetails(Specification<Project> spec, Pageable pageable);

    // Verifica se um usuário está envolvido em um projeto (manager ou membro)
    @Query("""
        SELECT COUNT(p) > 0 FROM Project p 
        LEFT JOIN p.team tm 
        WHERE p.id = :projectId 
        AND (p.manager.id = :userId OR tm.user.id = :userId)
    """)
    boolean isUserInvolvedInProject(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
    
    // Busca projetos onde o usuário é manager
    @Query("SELECT p FROM Project p WHERE p.manager.id = :userId")
    List<Project> findByManagerId(@Param("userId") UUID userId);
    
    // Busca projetos onde o usuário é membro da equipe
    @Query("""
        SELECT DISTINCT p FROM Project p 
        JOIN p.team tm 
        WHERE tm.user.id = :userId
    """)
    List<Project> findByTeamMemberId(@Param("userId") UUID userId);

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(
        String name,
        UUID id
    );

    long countByStatus(Project.ProjectStatus status);
    @Query("SELECT SUM(p.budget) FROM Project p")
    BigDecimal sumBudget();

    @Query("""
        SELECT COUNT(DISTINCT p)
        FROM Project p
        JOIN p.team m
        WHERE m.user.id = :userId
    """)
    long countByUser(@Param("userId") UUID userId);

    @Query("""
          SELECT COUNT(DISTINCT p)
          FROM Project p
          JOIN p.team m
          WHERE m.user.id = :userId 
          AND p.status = :status  
    """)
    long countByUserAndStatus(@Param("userId") UUID userId, @Param("status") ProjectStatus status);

    @Query("""
        SELECT new com.fransebastiao.taskmanager.dto.response.NameAndDescriptionResponse(
            p.id,
            p.name,
            p.description
        )      
        FROM Project p
    """)
    List<NameAndDescriptionResponse> findProjectNames();
}
