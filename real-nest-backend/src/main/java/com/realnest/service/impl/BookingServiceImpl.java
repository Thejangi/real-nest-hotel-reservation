package com.realnest.service.impl;

import com.realnest.entity.Booking;
import com.realnest.dto.BookingStats;
import com.realnest.repository.BookingRepository;
import com.realnest.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByBookingStatus(status);
    }

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Booking not found with id: " + id));
    }

    @Override
    public Booking updateBookingStatus(Long id, String status) {
        Booking booking = getBookingById(id);
        booking.setBookingStatus(status);
        return bookingRepository.save(booking);
    }

    @Override
    public BookingStats getBookingStatistics() {
        List<Booking> allBookings = getAllBookings();

        // Count active blocks (confirmed + bulk_booking)
        int activeBlocks = (int) allBookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getBookingStatus()) || "BULK_BOOKING".equals(b.getBookingStatus()))
                .count();

        // Since each booking typically represents 1 room, total rooms = total bookings
        int totalRooms = allBookings.size();

        // Calculate total revenue
        BigDecimal totalRevenue = allBookings.stream()
                .map(booking -> booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Count by status
        long confirmedBookings = allBookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getBookingStatus()))
                .count();

        long pendingBookings = allBookings.stream()
                .filter(b -> "PENDING".equals(b.getBookingStatus()) || "pending".equals(b.getBookingStatus()))
                .count();

        return new BookingStats(
                activeBlocks,
                totalRooms,
                totalRevenue,
                0.0, // averageDiscount - calculate from travel agencies if needed
                (int) confirmedBookings,
                (int) pendingBookings);
    }
}