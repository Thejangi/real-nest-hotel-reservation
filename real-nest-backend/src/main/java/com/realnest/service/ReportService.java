package com.realnest.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {

    Map<String, Object> getBookingSummary(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getNoShowReport(LocalDate date);

    List<Map<String, Object>> getDailyRevenueTrend(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getPaymentStats(LocalDate startDate, LocalDate endDate);
}
