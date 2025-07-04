package com.realnest.service.impl;

import com.realnest.entity.Booking;
import com.realnest.entity.Payment;
import com.realnest.repository.BookingRepository;
import com.realnest.repository.PaymentRepository;
import com.realnest.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public Map<String, Object> getBookingSummary(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByArrivalDateBetween(startDate, endDate);
        long total = bookings.size();
        long checkedIn = bookings.stream().filter(b -> "CHECKED_IN".equalsIgnoreCase(b.getBookingStatus())).count();
        long noShow = bookings.stream().filter(b -> "NO_SHOW".equalsIgnoreCase(b.getBookingStatus())).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalBookings", total);
        summary.put("checkedIn", checkedIn);
        summary.put("noShow", noShow);
        return summary;
    }

    @Override
    public Map<String, Object> getNoShowReport(LocalDate date) {
        List<Booking> noShows = bookingRepository.findByArrivalDateAndBookingStatus(date, "NO_SHOW");

        Map<String, Object> report = new HashMap<>();
        report.put("date", date);
        report.put("noShowCount", noShows.size());
        report.put("noShowDetails", noShows);
        return report;
    }

    @Override
    public List<Map<String, Object>> getDailyRevenueTrend(LocalDate startDate, LocalDate endDate) {
        List<Payment> payments = paymentRepository.findByPaymentDateBetween(startDate, endDate);

        return payments.stream()
                .collect(Collectors.groupingBy(Payment::getPaymentDate,
                        Collectors.summingDouble(p -> p.getAmount().doubleValue())))
                .entrySet()
                .stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", entry.getKey());
                    map.put("revenue", entry.getValue());
                    return map;
                })
                .sorted(Comparator.comparing(map -> (LocalDate) map.get("date")))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getPaymentStats(LocalDate startDate, LocalDate endDate) {
        List<Payment> payments = paymentRepository.findByPaymentDateBetween(startDate, endDate);

        double totalAmount = payments.stream()
                .mapToDouble(p -> p.getAmount().doubleValue())
                .sum();

        int count = payments.size();

        return Map.of(
                "totalPayments", count,
                "totalAmount", totalAmount);
    }

}
