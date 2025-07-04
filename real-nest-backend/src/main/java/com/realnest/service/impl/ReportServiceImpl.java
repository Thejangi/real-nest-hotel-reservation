package com.realnest.service.impl;

import com.realnest.entity.Booking;
import com.realnest.repository.BookingRepository;
import com.realnest.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private BookingRepository bookingRepository;

    // Block booking specific reports (no interface conflicts)

    @Override
    public List<Booking> getBlockBookingsByDateRange(LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findByCheckInDateBetween(startDate, endDate).stream()
                .filter(Booking::isBlockBooking)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getBlockBookingRevenue(LocalDate startDate, LocalDate endDate) {
        return getBlockBookingsByDateRange(startDate, endDate).stream()
                .filter(booking -> booking.getBookingStatus() == Booking.BookingStatus.CONFIRMED)
                .map(booking -> booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Double getAverageBlockBookingDiscount(LocalDate startDate, LocalDate endDate) {
        List<Booking> blockBookings = getBlockBookingsByDateRange(startDate, endDate);

        if (blockBookings.isEmpty()) {
            return 0.0;
        }

        return blockBookings.stream()
                .filter(booking -> booking.getDiscountPercentage() != null)
                .mapToDouble(booking -> booking.getDiscountPercentage().doubleValue())
                .average()
                .orElse(0.0);
    }

    @Override
    public List<Booking> getBookingsByDateRange(LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findByCheckInDateBetween(startDate, endDate);
    }

    @Override
    public BigDecimal getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByCheckInDateBetween(startDate, endDate);
        return bookings.stream()
                .filter(booking -> booking.getBookingStatus() == Booking.BookingStatus.CONFIRMED)
                .map(booking -> booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Booking> getTodaysCheckIns() {
        LocalDate today = LocalDate.now();
        return bookingRepository.findByCheckInDateAndBookingStatus(today, Booking.BookingStatus.CONFIRMED);
    }

    @Override
    public List<Booking> getOverdueBookings() {
        LocalDate currentDate = LocalDate.now();
        return bookingRepository.findOverdueBookings(currentDate);
    }

    @Override
    public Long getBookingCount(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByCheckInDateBetween(startDate, endDate);
        return (long) bookings.size();
    }

    @Override
    public Long getConfirmedBookingCount(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByCheckInDateBetween(startDate, endDate);
        return bookings.stream()
                .filter(booking -> booking.getBookingStatus() == Booking.BookingStatus.CONFIRMED)
                .count();
    }

    @Override
    public BigDecimal getTotalOutstandingAmount() {
        List<Booking> allBookings = bookingRepository.findAll();
        return allBookings.stream()
                .map(Booking::getRemainingAmount)
                .filter(amount -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Missing methods that ReportController needs
    @Override
    public Map<String, Object> getBookingSummary(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = getBookingsByDateRange(startDate, endDate);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalBookings", (long) bookings.size());
        summary.put("confirmedBookings", getConfirmedBookingCount(startDate, endDate));
        summary.put("totalRevenue", getTotalRevenue(startDate, endDate));
        summary.put("blockBookings", (long) bookings.stream()
                .filter(Booking::isBlockBooking)
                .count());
        summary.put("averageBookingValue",
                bookings.isEmpty() ? BigDecimal.ZERO
                        : getTotalRevenue(startDate, endDate).divide(BigDecimal.valueOf(bookings.size()), 2,
                                BigDecimal.ROUND_HALF_UP));

        return summary;
    }

    @Override
    public Map<String, Object> getNoShowReport(LocalDate date) {
        List<Booking> noShowBookings = bookingRepository.findByCheckInDateAndBookingStatus(date,
                Booking.BookingStatus.NO_SHOW);

        Map<String, Object> report = new HashMap<>();
        report.put("date", date);
        report.put("noShowBookings", noShowBookings);
        report.put("totalNoShows", (long) noShowBookings.size());
        report.put("lostRevenue", noShowBookings.stream()
                .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return report;
    }

    @Override
    public Map<String, Object> getPaymentStats(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = getBookingsByDateRange(startDate, endDate);

        long paidBookings = bookings.stream()
                .filter(b -> b.getPaymentStatus() == Booking.PaymentStatus.PAID)
                .count();

        long pendingBookings = bookings.stream()
                .filter(b -> b.getPaymentStatus() == Booking.PaymentStatus.PENDING)
                .count();

        long partialBookings = bookings.stream()
                .filter(b -> b.getPaymentStatus() == Booking.PaymentStatus.PARTIAL)
                .count();

        BigDecimal totalPaid = bookings.stream()
                .map(b -> b.getAdvancePaidAmount() != null ? b.getAdvancePaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> stats = new HashMap<>();
        stats.put("paidBookings", paidBookings);
        stats.put("pendingBookings", pendingBookings);
        stats.put("partialBookings", partialBookings);
        stats.put("totalPaidAmount", totalPaid);
        stats.put("outstandingAmount", getTotalOutstandingAmount());

        return stats;
    }

    @Override
    public Map<String, Object> getDailyRevenueTrend(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> trend = new HashMap<>();

        // Simple implementation - in a real system you'd calculate daily breakdown
        BigDecimal totalRevenue = getTotalRevenue(startDate, endDate);
        Long totalDays = (long) startDate.until(endDate).getDays() + 1;

        BigDecimal averageDailyRevenue = totalDays > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalDays), 2, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;

        trend.put("startDate", startDate);
        trend.put("endDate", endDate);
        trend.put("totalRevenue", totalRevenue);
        trend.put("averageDailyRevenue", averageDailyRevenue);
        trend.put("totalDays", totalDays);

        return trend;
    }
}