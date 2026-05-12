package com.fransebastiao.taskmanager.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.task.Task;
import com.fransebastiao.taskmanager.domain.task.Task.TaskStatus;
import com.fransebastiao.taskmanager.domain.user.User;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task>  {
    List<Task> findByProjectId(UUID projectId);

    List<Task> findByProjectIdAndStatus(UUID projectId, Task.TaskStatus status);

    @Query("""
        SELECT DISTINCT t FROM Task t
        JOIN t.assignees a
        WHERE a.id = :userId
        """)
    List<Task> findByAssigneeId(@Param("userId") UUID userId);

    @Query("""
        SELECT t FROM Task t
        LEFT JOIN FETCH t.laborEntries
        WHERE t.id = :id
        """)
    Optional<Task> findByIdWithLaborEntries(@Param("id") UUID id);

    @Query("""
        SELECT t FROM Task t
        WHERE t.dueDate < :date
        AND t.status NOT IN ('COMPLETED')
        """)
    List<Task> findOverdueTasks(@Param("date") LocalDate date);

    @Query("""
        SELECT t FROM Task t
        LEFT JOIN FETCH t.assignees
        WHERE t.id = :id
        """)
    Optional<Task> findByIdWithAssignees(@Param("id") UUID id);

    @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN FETCH t.assignees
        WHERE t.project.id = :projectId
    """)
    Page<Task> findByProjectIdWithAssignees(@Param("projectId") UUID projectId, Pageable pageable);

    @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN FETCH t.assignees
        LEFT JOIN FETCH t.category
        WHERE t.project.id = :projectId
        """)
    List<Task> findByProjectIdWithAssignees(@Param("projectId") UUID projectId);

    @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN FETCH t.assignees a
        LEFT JOIN FETCH t.category
        WHERE a.id = :userId
        """)
    List<Task> findByAssigneeIdWithDetails(@Param("userId") UUID userId);

    @Query("SELECT COUNT(t) > 0 FROM Task t JOIN t.assignees a WHERE t.id = :taskId AND a.id = :userId")
    boolean isUserAssignedToTask(@Param("taskId") UUID taskId, @Param("userId") UUID userId);

    boolean existsByProjectIdAndTitleIgnoreCase(UUID projectId, String title);
    boolean existsByProjectIdAndTitleIgnoreCaseAndIdNot(
        UUID projectId,
        String title,
        UUID id
    );

    long countByStatus(Task.TaskStatus status);
    @Query("""
        SELECT COUNT(t) FROM Task t
        WHERE t.dueDate < :today
        AND t.status != 'COMPLETA'
    """)
    long countOverdue(@Param("today") LocalDate today);

    @Query("""
        SELECT COUNT(t)
        FROM Task t   
        JOIN t.assignees a
        WHERE a.id = :userId     
    """)
    long countByAssignee(@Param("userId") UUID userId);

    @Query("""
        SELECT COUNT(t)
        FROM Task t
        JOIN t.assignees a
        WHERE a.id = :userId
        AND t.status = :status        
    """)
    long countByAssigneeAndStatus(@Param("userId") UUID userId, @Param("status") TaskStatus status);

    @Query("""
       SELECT COUNT(t)
       FROM Task t
       JOIN t.assignees a
       WHERE a.id = :userId
       AND t.dueDate < :date
       AND t.status <> :completedStatus     
    """)
    long countOverdueByAssignee(@Param("userId") UUID userId, @Param("date") LocalDate date, @Param("completedStatus") TaskStatus completedStatus);

    Page<Task> findByProjectId(UUID projectId, Pageable pageable);

    @Query("""
        SELECT AVG(t.progressPercent)
        FROM Task t
        WHERE t.project.id = :projectId
    """)
    Double getAverageProgress(UUID projectId);

    Optional<Task> findById(UUID id);

    @Query("""
        SELECT u FROM Task t
        JOIN t.assignees u
        WHERE t.id = :taskId        
    """)
    Page<User> findAssigneesByTaskId(@Param("taskId") UUID taskId, Pageable pageable);

    boolean existsByTitleIgnoreCase(String title);
    boolean existsByTitleIgnoreCaseAndIdNot(String title, UUID id);

    @Query(""" 
        SELECT t 
        FROM Task t 
        WHERE t.dueDate >= CURRENT_DATE 
        ORDER BY t.dueDate ASC 
    """) 
    Page<Task> findUpcomingTasks(Pageable pageable); 
    
    @Query("""
        SELECT t FROM Task t
        JOIN t.assignees u
        WHERE u.id = :userId
        AND t.dueDate >= CURRENT_DATE
        ORDER BY t.dueDate ASC
    """)
    Page<Task> findUpcomingTasksByAssignee(@Param(" userId") UUID userId, Pageable pageable);

    Page<Task> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
        SELECT DISTINCT t FROM Task t
        LEFT JOIN FETCH t.assignees a
        WHERE a.id = :userId
    """)
    Page<Task> findByAssignee(UUID userId, Pageable pageable);

    long countByProjectId(UUID projectId);

    boolean existsByParentTaskAndTitleIgnoreCase(Task parentTask, String title);
}