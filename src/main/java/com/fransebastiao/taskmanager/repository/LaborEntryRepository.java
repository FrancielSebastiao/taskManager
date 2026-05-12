package com.fransebastiao.taskmanager.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.task.LaborEntry;

@Repository
public interface LaborEntryRepository extends JpaRepository<LaborEntry, UUID> {

    List<LaborEntry> findByTaskId(UUID taskId);

    List<LaborEntry> findByWorkerId(UUID workerId);

    @Query("""
        SELECT l FROM LaborEntry l
        WHERE l.task.project.id = :projectId
        """)
    List<LaborEntry> findByProjectId(@Param("projectId") UUID projectId);

    @Query("""
        SELECT l FROM LaborEntry l
        WHERE l.worker.id = :workerId
        AND l.actualEndDate IS NULL
        """)
    List<LaborEntry> findPendingByWorkerId(@Param("workerId") UUID workerId);

    @Query("""
        SELECT l FROM LaborEntry l
        WHERE l.task.project.id = :projectId
        AND l.actualEndDate IS NOT NULL
        """)
    List<LaborEntry> findCompletedByProjectId(@Param("projectId") UUID projectId);

    @Query("""
        SELECT SUM(l.agreedAmount) FROM LaborEntry l
        WHERE l.actualEndDate IS NOT NULL
    """)
    BigDecimal sumFinalAmounts();
}
