package com.realnest.service;

import com.realnest.entity.Booking;
import com.realnest.dto.BookingStats;
import java.util.List;

public interface BookingService {
    List<Booking> getAllBookings();

    List<Booking> getBookingsByStatus(String status);

    Booking getBookingById(Long id);

    Booking updateBookingStatus(Long id, String status);

    BookingStats getBookingStatistics();
}