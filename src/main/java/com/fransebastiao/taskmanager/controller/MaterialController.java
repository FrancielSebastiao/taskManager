package com.fransebastiao.taskmanager.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.domain.resource.Material;
import com.fransebastiao.taskmanager.dto.request.CreateMaterialRequest;
import com.fransebastiao.taskmanager.dto.request.CreateMaterialUsageRequest;
import com.fransebastiao.taskmanager.dto.response.MaterialUsageResponse;
import com.fransebastiao.taskmanager.service.MaterialService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/materials")
@RequiredArgsConstructor
@Validated
public class MaterialController {

    private final MaterialService materialService;

    @PostMapping
    @PreAuthorize("hasAuthority('CRIAR_MATERIAIS')")
    public ResponseEntity<Material> criarMaterial(
            @RequestBody @Valid CreateMaterialRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(materialService.criarMaterial(request));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Material>> pesquisar(@RequestParam String nome) {
        return ResponseEntity.ok(materialService.pesquisar(nome));
    }

    @PostMapping("/usage")
    @PreAuthorize("hasAuthority('REGISTAR_USO_DE_MATERIAL')")
    public ResponseEntity<MaterialUsageResponse> registarUso(
            @RequestBody @Valid CreateMaterialUsageRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(materialService.toResponse(materialService.registarUso(request)));
    }

    @GetMapping("/usage/project/{projectId}")
    public ResponseEntity<List<MaterialUsageResponse>> listarUsoPorProjecto(
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(materialService.listarUsoPorProjecto(projectId));
    }

    @GetMapping("/usage/project/{projectId}/range")
    public ResponseEntity<List<MaterialUsageResponse>> listarUsoPorProjectoEPeriodo(
            @PathVariable UUID projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(
                materialService.listarUsoPorProjectoEPeriodo(projectId, from, to));
    }

    @GetMapping("/usage/project/{projectId}/total-cost")
    @PreAuthorize("hasAuthority('LER_CUSTOS')")
    public ResponseEntity<Map<String, BigDecimal>> calcularCustoTotal(
            @PathVariable UUID projectId) {
        BigDecimal total = materialService.calcularCustoTotalMateriais(projectId);
        return ResponseEntity.ok(Map.of("totalMaterialCost", total));
    }
}