package com.realnest.controller;

import com.realnest.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getBookingSummary(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> summary = reportService.getBookingSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/no-shows")
    public ResponseEntity<Map<String, Object>> getNoShowReport(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Map<String, Object> report = reportService.getNoShowReport(date);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/payments")
    public ResponseEntity<Map<String, Object>> getPaymentStats(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<String, Object> stats = reportService.getPaymentStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/revenue-trend")
    public ResponseEntity<?> getDailyRevenueTrend(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(reportService.getDailyRevenueTrend(startDate, endDate));
    }
}
