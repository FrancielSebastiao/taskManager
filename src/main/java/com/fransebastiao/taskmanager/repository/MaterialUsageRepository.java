package com.fransebastiao.taskmanager.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fransebastiao.taskmanager.domain.resource.MaterialUsage;

@Repository
public interface MaterialUsageRepository extends JpaRepository<MaterialUsage, UUID> {

    List<MaterialUsage> findByProjectId(UUID projectId);

    List<MaterialUsage> findByMaterialId(UUID materialId);

    @Query("""
        SELECT m FROM MaterialUsage m
        LEFT JOIN FETCH m.material
        WHERE m.project.id = :projectId
        """)
    List<MaterialUsage> findByProjectIdWithMaterial(@Param("projectId") UUID projectId);

    @Query("""
        SELECT m FROM MaterialUsage m
        WHERE m.project.id = :projectId
        AND m.usageDate BETWEEN :from AND :to
        """)
    List<MaterialUsage> findByProjectIdAndDateRange(
            @Param("projectId") UUID projectId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
        SELECT m FROM MaterialUsage m
        LEFT JOIN FETCH m.material
        LEFT JOIN FETCH m.project
        WHERE m.recordedBy.id = :userId
        AND m.usageDate BETWEEN :from AND :to
        """)
    List<MaterialUsage> findByRecordedByIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT SUM(m.quantityUsed * m.material.unitPrice) FROM MaterialUsage m")
    BigDecimal sumTotalCosts();
}
