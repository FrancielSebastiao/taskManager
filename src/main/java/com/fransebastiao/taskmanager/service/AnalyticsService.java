package com.fransebastiao.taskmanager.service;

import java.time.LocalDate;

import com.fransebastiao.taskmanager.dto.response.AnalyticsDashboardResponse;

public interface AnalyticsService {
    AnalyticsDashboardResponse getDashboard(LocalDate from, LocalDate to);
}
