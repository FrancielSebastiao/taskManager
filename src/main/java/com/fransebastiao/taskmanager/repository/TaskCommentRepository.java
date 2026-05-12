package com.fransebastiao.taskmanager.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.task.TaskComment;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

    List<TaskComment> findByTaskIdOrderByCreatedAtDesc(UUID taskId);

    List<TaskComment> findByTaskIdAndCategory(
            UUID taskId, TaskComment.CommentCategory category);

    List<TaskComment> findByAuthorId(UUID authorId);

    @Query("""
        SELECT c FROM TaskComment c
        WHERE c.task.project.id = :projectId
        ORDER BY c.createdAt DESC
        """)
    List<TaskComment> findByProjectId(@Param("projectId") UUID projectId);

    @Query("""
        SELECT tc
        FROM TaskComment tc
        LEFT JOIN FETCH tc.attachmentFile af
        WHERE tc.task.id = :taskId
    """)
    Page<TaskComment> findByTaskId(UUID taskId, Pageable pageable);
}
