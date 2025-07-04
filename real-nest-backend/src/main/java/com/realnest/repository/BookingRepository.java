package com.realnest.repository;

import com.realnest.entity.Booking;
import com.realnest.entity.TravelAgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Status-based queries
    List<Booking> findByBookingStatus(String bookingStatus);

    List<Booking> findByPaymentStatus(String paymentStatus);

    // Travel agency queries
    List<Booking> findByTravelAgencyId(Long travelAgencyId);

    List<Booking> findByTravelAgency(TravelAgency travelAgency);

    // Date-based queries using actual database fields
    List<Booking> findByArrivalDateBetween(LocalDate startDate, LocalDate endDate);

    List<Booking> findByArrivalDateAndBookingStatus(LocalDate arrivalDate, String bookingStatus);

    List<Booking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);

    List<Booking> findByCheckInDateAndBookingStatus(LocalDate checkInDate, String bookingStatus);

    // Guest-based queries
    List<Booking> findByGuestEmail(String guestEmail);

    List<Booking> findByGuestName(String guestName);

    // Room-based queries
    List<Booking> findByRoomId(Long roomId);

    List<Booking> findByRoomType(String roomType);
}