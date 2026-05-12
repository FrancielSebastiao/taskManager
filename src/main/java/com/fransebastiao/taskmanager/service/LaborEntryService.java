package com.fransebastiao.taskmanager.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fransebastiao.taskmanager.domain.task.LaborEntry;
import com.fransebastiao.taskmanager.dto.request.CreateLaborEntryRequest;
import com.fransebastiao.taskmanager.dto.response.LaborEntryResponse;

public interface LaborEntryService {
    LaborEntry criar(CreateLaborEntryRequest request);
    LaborEntry concluir(UUID id, LocalDate actualEndDate);
    LaborEntry buscarPorId(UUID id);
    List<LaborEntryResponse> listarPorProjecto(UUID projectId);
    List<LaborEntryResponse> listarPendentesPorWorker(UUID workerId);
    BigDecimal calcularCustoTotalProjecto(UUID projectId);
    LaborEntryResponse toResponse(LaborEntry e);
}
