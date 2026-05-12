package com.fransebastiao.taskmanager.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fransebastiao.taskmanager.domain.project.Project;
import com.fransebastiao.taskmanager.domain.resource.Material;
import com.fransebastiao.taskmanager.domain.resource.MaterialUsage;
import com.fransebastiao.taskmanager.domain.user.User;
import com.fransebastiao.taskmanager.dto.request.CreateMaterialRequest;
import com.fransebastiao.taskmanager.dto.request.CreateMaterialUsageRequest;
import com.fransebastiao.taskmanager.dto.response.MaterialUsageResponse;
import com.fransebastiao.taskmanager.repository.MaterialRepository;
import com.fransebastiao.taskmanager.repository.MaterialUsageRepository;
import com.fransebastiao.taskmanager.repository.ProjectRepository;
import com.fransebastiao.taskmanager.repository.UserRepository;
import com.fransebastiao.taskmanager.service.MaterialService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository      materialRepository;
    private final MaterialUsageRepository materialUsageRepository;
    private final ProjectRepository       projectRepository;
    private final UserRepository          userRepository;

    public Material criarMaterial(CreateMaterialRequest request) {
        if (materialRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Material already exists: " + request.name());
        }
        Material material = new Material(request.name(), request.unit(), request.unitPrice());
        log.info("Creating material: {}", material.getName());
        return materialRepository.save(material);
    }

    public MaterialUsage registarUso(CreateMaterialUsageRequest request) {
        Project  project  = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        Material material = materialRepository.findById(request.materialId())
                .orElseThrow(() -> new EntityNotFoundException("Material not found"));

        MaterialUsage usage = new MaterialUsage(
                project, material,
                request.quantityUsed(),
                request.usageDate()
        );
        usage.setNotes(request.notes());

        if (request.recordedById() != null) {
            User recordedBy = userRepository.findById(request.recordedById())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            usage.setRecordedBy(recordedBy);
        }

        log.info("Material usage registered: {} x{} on project {}",
                material.getName(), request.quantityUsed(), request.projectId());
        return materialUsageRepository.save(usage);
    }

    @Transactional(readOnly = true)
    public List<MaterialUsageResponse> listarUsoPorProjecto(UUID projectId) {
        return materialUsageRepository.findByProjectIdWithMaterial(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MaterialUsageResponse> listarUsoPorProjectoEPeriodo(
            UUID projectId, LocalDate from, LocalDate to) {
        return materialUsageRepository.findByProjectIdAndDateRange(projectId, from, to).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularCustoTotalMateriais(UUID projectId) {
        return materialUsageRepository.findByProjectIdWithMaterial(projectId).stream()
                .map(MaterialUsage::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<Material> pesquisar(String nome) {
        return materialRepository.findByNameContainingIgnoreCase(nome);
    }

    public MaterialUsageResponse toResponse(MaterialUsage u) {
        return new MaterialUsageResponse(
                u.getId(),
                u.getMaterial().getName(),
                u.getMaterial().getUnit(),
                u.getQuantityUsed(),
                u.getMaterial().getUnitPrice(),
                u.getTotalCost(),
                u.getUsageDate(),
                u.getNotes()
        );
    }
}
