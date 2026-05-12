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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.dto.request.CreateLaborEntryRequest;
import com.fransebastiao.taskmanager.dto.response.LaborEntryResponse;
import com.fransebastiao.taskmanager.service.LaborEntryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/labor-entries")
@RequiredArgsConstructor
@Validated
public class LaborEntryController {

    private final LaborEntryService laborEntryService;

    @PostMapping
    @PreAuthorize("hasAuthority('CRIAR_REGISTRO_DE_PRODUCAO')")
    public ResponseEntity<LaborEntryResponse> criar(
            @RequestBody @Valid CreateLaborEntryRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(laborEntryService.toResponse(laborEntryService.criar(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaborEntryResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(laborEntryService.toResponse(laborEntryService.buscarPorId(id)));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<LaborEntryResponse>> listarPorProjecto(
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(laborEntryService.listarPorProjecto(projectId));
    }

    @GetMapping("/worker/{workerId}/pending")
    public ResponseEntity<List<LaborEntryResponse>> listarPendentesPorWorker(
            @PathVariable UUID workerId) {
        return ResponseEntity.ok(laborEntryService.listarPendentesPorWorker(workerId));
    }

    @GetMapping("/project/{projectId}/total-cost")
    public ResponseEntity<Map<String, BigDecimal>> calcularCustoTotal(
            @PathVariable UUID projectId) {
        BigDecimal total = laborEntryService.calcularCustoTotalProjecto(projectId);
        return ResponseEntity.ok(Map.of("totalLaborCost", total));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('COMPLETAR_REGISTRO_DE_PRODUCAO')")
    public ResponseEntity<LaborEntryResponse> concluir(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate actualEndDate) {
        return ResponseEntity.ok(
                laborEntryService.toResponse(laborEntryService.concluir(id, actualEndDate)));
    }
}
