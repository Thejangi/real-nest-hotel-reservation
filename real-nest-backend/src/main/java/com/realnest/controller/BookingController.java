package com.realnest.controller;

import com.realnest.entity.Booking;
import com.realnest.dto.BookingStats;
import com.realnest.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BookingController handles all API endpoints related to hotel bookings.
 * Supports CRUD operations and is CORS-enabled for frontend consumption.
 */
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Get all bookings (for dashboard)
    @GetMapping
    public List<Booking> getAllBookings(@RequestParam(required = false) String status) {
        if (status != null) {
            return bookingService.getBookingsByStatus(status);
        }
        return bookingService.getAllBookings();
    }

    // Get booking by ID
    @GetMapping("/{id}")
    public Booking getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id);
    }

    // Update booking status
    @PutMapping("/{id}/status")
    public Booking updateBookingStatus(@PathVariable Long id, @RequestParam String status) {
        return bookingService.updateBookingStatus(id, status);
    }

    // Get booking statistics
    @GetMapping("/stats")
    public BookingStats getBookingStatistics() {
        return bookingService.getBookingStatistics();
    }
}