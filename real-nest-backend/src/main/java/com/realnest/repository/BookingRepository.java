package com.realnest.repository;

import com.realnest.entity.Booking;
import com.realnest.entity.TravelAgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find by booking reference
    Optional<Booking> findByBookingReference(String bookingReference);

    // Status-based queries using enums
    List<Booking> findByBookingStatus(Booking.BookingStatus bookingStatus);

    List<Booking> findByPaymentStatus(Booking.PaymentStatus paymentStatus);

    // Travel agency queries
    List<Booking> findByTravelAgencyId(Long travelAgencyId);

    List<Booking> findByTravelAgency(TravelAgency travelAgency);

    // Date-based queries
    List<Booking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);

    List<Booking> findByCheckOutDateBetween(LocalDate startDate, LocalDate endDate);

    List<Booking> findByCheckInDateAndBookingStatus(LocalDate checkInDate, Booking.BookingStatus bookingStatus);

    // Block booking specific queries
    @Query("SELECT b FROM Booking b WHERE b.numberOfRooms >= 3")
    List<Booking> findAllBlockBookings();

    @Query("SELECT b FROM Booking b WHERE b.numberOfRooms >= 3 AND b.travelAgencyId = :agencyId")
    List<Booking> findBlockBookingsByAgency(@Param("agencyId") Long agencyId);

    @Query("SELECT b FROM Booking b WHERE b.numberOfRooms >= 3 AND b.bookingStatus = :status")
    List<Booking> findBlockBookingsByStatus(@Param("status") Booking.BookingStatus status);

    // Block reference queries
    List<Booking> findByBlockReference(String blockReference);

    List<Booking> findByBlockReferenceAndTravelAgencyId(String blockReference, Long travelAgencyId);

    // Payment related queries
    @Query("SELECT b FROM Booking b WHERE b.paymentStatus = 'PENDING' AND b.checkInDate = :today")
    List<Booking> findPendingPaymentsForToday(@Param("today") LocalDate today);

    @Query("SELECT b FROM Booking b WHERE b.paymentStatus = 'OVERDUE' OR (b.paymentStatus = 'PENDING' AND b.checkInDate < :currentDate)")
    List<Booking> findOverdueBookings(@Param("currentDate") LocalDate currentDate);

    // Room distribution queries
    @Query("SELECT SUM(b.numberOfRooms) FROM Booking b WHERE b.checkInDate <= :date AND b.checkOutDate > :date AND b.bookingStatus = 'CONFIRMED'")
    Integer getTotalRoomsOccupiedOnDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(b.standardRooms) FROM Booking b WHERE b.checkInDate <= :date AND b.checkOutDate > :date AND b.bookingStatus = 'CONFIRMED'")
    Integer getStandardRoomsOccupiedOnDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(b.deluxeRooms) FROM Booking b WHERE b.checkInDate <= :date AND b.checkOutDate > :date AND b.bookingStatus = 'CONFIRMED'")
    Integer getDeluxeRoomsOccupiedOnDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(b.suiteRooms) FROM Booking b WHERE b.checkInDate <= :date AND b.checkOutDate > :date AND b.bookingStatus = 'CONFIRMED'")
    Integer getSuiteRoomsOccupiedOnDate(@Param("date") LocalDate date);

    // Revenue queries
    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.travelAgencyId = :agencyId AND b.bookingStatus = 'CONFIRMED'")
    BigDecimal getTotalRevenueByAgency(@Param("agencyId") Long agencyId);

    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE b.checkInDate BETWEEN :startDate AND :endDate AND b.bookingStatus = 'CONFIRMED'")
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(b.advancePaidAmount) FROM Booking b WHERE b.travelAgencyId = :agencyId")
    BigDecimal getTotalAdvancePaidByAgency(@Param("agencyId") Long agencyId);

    // Statistics queries
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.numberOfRooms >= 3 AND b.bookingStatus = 'CONFIRMED'")
    Long countActiveBlockBookings();

    @Query("SELECT AVG(b.discountPercentage) FROM Booking b WHERE b.travelAgencyId = :agencyId AND b.numberOfRooms >= 3")
    Double getAverageDiscountByAgency(@Param("agencyId") Long agencyId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.travelAgencyId = :agencyId AND b.numberOfRooms >= 3")
    Long countBlockBookingsByAgency(@Param("agencyId") Long agencyId);

    // Search queries
    @Query("SELECT b FROM Booking b WHERE " +
            "(LOWER(b.bookingReference) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.blockReference) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(b.travelAgency.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "b.numberOfRooms >= 3")
    List<Booking> searchBlockBookings(@Param("searchTerm") String searchTerm);

    // Date range with multiple filters
    @Query("SELECT b FROM Booking b WHERE " +
            "b.numberOfRooms >= 3 AND " +
            "(:agencyId IS NULL OR b.travelAgencyId = :agencyId) AND " +
            "(:status IS NULL OR b.bookingStatus = :status) AND " +
            "(:paymentStatus IS NULL OR b.paymentStatus = :paymentStatus) AND " +
            "(:startDate IS NULL OR b.checkInDate >= :startDate) AND " +
            "(:endDate IS NULL OR b.checkInDate <= :endDate)")
    List<Booking> findBlockBookingsWithFilters(
            @Param("agencyId") Long agencyId,
            @Param("status") Booking.BookingStatus status,
            @Param("paymentStatus") Booking.PaymentStatus paymentStatus,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Bulk operations for travel agency service
    @Query("SELECT b FROM Booking b WHERE b.travelAgencyId = :agencyId AND b.checkInDate BETWEEN :startDate AND :endDate")
    List<Booking> findBookingsByAgencyAndDateRange(
            @Param("agencyId") Long agencyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}