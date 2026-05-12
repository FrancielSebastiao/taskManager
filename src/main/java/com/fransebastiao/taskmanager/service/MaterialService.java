package com.fransebastiao.taskmanager.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fransebastiao.taskmanager.domain.resource.Material;
import com.fransebastiao.taskmanager.domain.resource.MaterialUsage;
import com.fransebastiao.taskmanager.dto.request.CreateMaterialRequest;
import com.fransebastiao.taskmanager.dto.request.CreateMaterialUsageRequest;
import com.fransebastiao.taskmanager.dto.response.MaterialUsageResponse;

public interface MaterialService {
    Material criarMaterial(CreateMaterialRequest request);
    MaterialUsage registarUso(CreateMaterialUsageRequest request);
    List<MaterialUsageResponse> listarUsoPorProjecto(UUID projectId);
    List<MaterialUsageResponse> listarUsoPorProjectoEPeriodo(UUID projectId, LocalDate from, LocalDate to);
    BigDecimal calcularCustoTotalMateriais(UUID projectId);
    List<Material> pesquisar(String nome);
    MaterialUsageResponse toResponse(MaterialUsage u);
}
