package com.fransebastiao.taskmanager.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fransebastiao.taskmanager.dto.response.AnalyticsDashboardResponse;
import com.fransebastiao.taskmanager.dto.response.ReportData;
import com.fransebastiao.taskmanager.service.AnalyticsService;
import com.fransebastiao.taskmanager.service.ReportDataService;
import com.fransebastiao.taskmanager.service.impl.PdfReportService;
import com.fransebastiao.taskmanager.util.ReportPeriod;
import com.itextpdf.io.exceptions.IOException;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportDataService reportDataService;
    private final PdfReportService  pdfReportService;
    private final AnalyticsService  analyticsService;

    // -------------------------------------------------------------------------
    // Relatório individual PDF
    // -------------------------------------------------------------------------

    @GetMapping("/worker/{targetUserId}/pdf")
    @PreAuthorize("hasAuthority('LER_RELATORIOS')")
    public ResponseEntity<byte[]> generateWorkerReport(
            @PathVariable UUID targetUserId,
            @RequestParam UUID requesterId,
            @RequestParam ReportPeriod period,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to)
            throws IOException {

        LocalDate[] range = period.toDateRange(from, to);
        ReportData  data  = reportDataService.buildReportData(
                requesterId, targetUserId, range[0], range[1]);
        byte[]      pdf   = pdfReportService.generate(data);

        String filename = String.format("relatorio_%s_%s_%s.pdf",
                data.workerName().replace(" ", "_").toLowerCase(),
                range[0], range[1]);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }

    // -------------------------------------------------------------------------
    // Analytics dashboard com filtros de período
    // -------------------------------------------------------------------------

    @GetMapping("/analytics")
    @PreAuthorize("hasAuthority('LER_RELATORIOS')")
    public ResponseEntity<AnalyticsDashboardResponse> getAnalytics(
            @RequestParam(defaultValue = "LAST_30_DAYS") ReportPeriod period,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDate[] range = period.toDateRange(from, to);
        return ResponseEntity.ok(analyticsService.getDashboard(range[0], range[1]));
    }
}
