package com.realnest.service;

import com.realnest.entity.Booking;
import com.realnest.dto.BookingStats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    // Existing methods (keeping backward compatibility)
    List<Booking> getAllBookings();

    List<Booking> getBookingsByStatus(String status);

    Booking getBookingById(Long id);

    Booking updateBookingStatus(Long id, String status);

    BookingStats getBookingStatistics();

    // New block booking methods
    Booking createBlockBooking(Booking booking);

    Booking updateBlockBooking(Long id, Booking booking);

    void deleteBlockBooking(Long id);

    List<Booking> getAllBlockBookings();

    List<Booking> getBlockBookingsByAgency(Long agencyId);

    List<Booking> getBlockBookingsByStatus(Booking.BookingStatus status);

    Booking getBlockBookingByReference(String bookingReference);

    // Enhanced filtering and search
    List<Booking> getBlockBookingsWithFilters(
            Long agencyId,
            Booking.BookingStatus status,
            Booking.PaymentStatus paymentStatus,
            LocalDate startDate,
            LocalDate endDate);

    List<Booking> searchBlockBookings(String searchTerm);

    // Payment management
    Booking updatePaymentStatus(Long id, Booking.PaymentStatus paymentStatus, BigDecimal paidAmount);

    List<Booking> getPendingPayments();

    List<Booking> getOverdueBookings();

    BigDecimal getTotalOutstandingAmount(Long agencyId);

    // Room availability
    boolean checkRoomAvailability(LocalDate checkIn, LocalDate checkOut,
            Integer standardRooms, Integer deluxeRooms, Integer suiteRooms);

    Integer getTotalRoomsOccupiedOnDate(LocalDate date);

    // Statistics and reporting
    BookingStats getBlockBookingStatistics();

    BigDecimal getTotalRevenueByAgency(Long agencyId);

    BigDecimal getTotalRevenueByDateRange(LocalDate startDate, LocalDate endDate);

    Long countBlockBookingsByAgency(Long agencyId);

    Double getAverageDiscountByAgency(Long agencyId);

    // Bulk operations (for travel agency integration)
    List<Booking> createBulkBlockBookings(Long agencyId, List<Booking> bookings);

    List<Booking> getBookingsByAgencyAndDateRange(Long agencyId, LocalDate startDate, LocalDate endDate);

    // Pricing calculations
    Booking calculateBookingPricing(Booking booking);

    BigDecimal calculateAdvanceAmount(BigDecimal totalAmount, Booking.PaymentTerms paymentTerms);
}