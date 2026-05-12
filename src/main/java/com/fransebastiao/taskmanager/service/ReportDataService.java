package com.fransebastiao.taskmanager.service;

import java.time.LocalDate;
import java.util.UUID;

import com.fransebastiao.taskmanager.dto.response.ReportData;

public interface ReportDataService {
    ReportData buildReportData(UUID requesterId, UUID targetUserId,
                                       LocalDate from, LocalDate to);
}
