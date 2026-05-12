package com.fransebastiao.taskmanager.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Builder;

@Builder
public record ReportData(
    String              workerName,
    String              workerEmail,
    String              roleName,
    LocalDate           periodFrom,
    LocalDate           periodTo,

    // Desempenho individual
    long                totalTasks,
    long                completedTasks,
    long                inProgressTasks,
    long                pendingTasks,
    double              completionRate,
    double              avgCompletionDays,

    // Tarefas detalhadas
    List<TaskReportDto>         tasks,

    // Mão de obra
    List<LaborReportDto>        laborEntries,
    BigDecimal                  totalAgreed,
    BigDecimal                  totalFinal,
    BigDecimal                  totalBonus,
    BigDecimal                  totalDiscount,

    // Materiais
    List<MaterialReportDto>     materials,
    BigDecimal                  totalMaterialCost,

    // Custos totais
    BigDecimal                  totalProjectCost
) {}