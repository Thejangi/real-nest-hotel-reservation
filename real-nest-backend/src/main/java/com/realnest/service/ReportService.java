package com.realnest.service;

import com.realnest.entity.Booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {

    // Block booking reports
    List<Booking> getBlockBookingsByDateRange(LocalDate startDate, LocalDate endDate);

    BigDecimal getBlockBookingRevenue(LocalDate startDate, LocalDate endDate);

    Double getAverageBlockBookingDiscount(LocalDate startDate, LocalDate endDate);

    // Basic booking reports
    List<Booking> getBookingsByDateRange(LocalDate startDate, LocalDate endDate);

    BigDecimal getTotalRevenue(LocalDate startDate, LocalDate endDate);

    List<Booking> getTodaysCheckIns();

    List<Booking> getOverdueBookings();

    Long getBookingCount(LocalDate startDate, LocalDate endDate);

    Long getConfirmedBookingCount(LocalDate startDate, LocalDate endDate);

    BigDecimal getTotalOutstandingAmount();

    // Missing methods that ReportController needs
    Map<String, Object> getBookingSummary(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getNoShowReport(LocalDate date);

    Map<String, Object> getPaymentStats(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getDailyRevenueTrend(LocalDate startDate, LocalDate endDate);
}