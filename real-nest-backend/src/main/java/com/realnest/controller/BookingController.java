package com.realnest.controller;

import com.realnest.entity.Booking;
import com.realnest.dto.BookingStats;
import com.realnest.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // ============ EXISTING ENDPOINTS (Backward Compatibility) ============

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Booking>> getBookingsByStatus(@PathVariable String status) {
        try {
            List<Booking> bookings = bookingService.getBookingsByStatus(status);
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        try {
            Booking booking = bookingService.getBookingById(id);
            return new ResponseEntity<>(booking, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Booking> updateBookingStatus(@PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String status = statusUpdate.get("status");
            if (status == null || status.trim().isEmpty()) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            Booking updatedBooking = bookingService.updateBookingStatus(id, status);
            return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<BookingStats> getBookingStatistics() {
        try {
            BookingStats stats = bookingService.getBookingStatistics();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============ NEW BLOCK BOOKING ENDPOINTS ============

    @PostMapping("/block")
    public ResponseEntity<Booking> createBlockBooking(@RequestBody Booking booking) {
        try {
            // Validate minimum rooms for block booking
            if (booking.getNumberOfRooms() == null || booking.getNumberOfRooms() < 3) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }

            Booking createdBooking = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .createBlockBooking(booking);
            return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/block/{id}")
    public ResponseEntity<Booking> updateBlockBooking(@PathVariable Long id, @RequestBody Booking booking) {
        try {
            Booking updatedBooking = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .updateBlockBooking(id, booking);
            return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/block/{id}")
    public ResponseEntity<HttpStatus> deleteBlockBooking(@PathVariable Long id) {
        try {
            ((com.realnest.service.impl.BookingServiceImpl) bookingService).deleteBlockBooking(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/block")
    public ResponseEntity<List<Booking>> getAllBlockBookings() {
        try {
            List<Booking> blockBookings = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .getAllBlockBookings();
            return new ResponseEntity<>(blockBookings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/block/agency/{agencyId}")
    public ResponseEntity<List<Booking>> getBlockBookingsByAgency(@PathVariable Long agencyId) {
        try {
            List<Booking> blockBookings = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .getBlockBookingsByAgency(agencyId);
            return new ResponseEntity<>(blockBookings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/block/reference/{reference}")
    public ResponseEntity<Booking> getBlockBookingByReference(@PathVariable String reference) {
        try {
            Booking booking = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .getBlockBookingByReference(reference);
            return new ResponseEntity<>(booking, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // ============ FILTERING AND SEARCH ============

    @GetMapping("/block/search")
    public ResponseEntity<List<Booking>> getBlockBookingsWithFilters(
            @RequestParam(required = false) Long agencyId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String searchTerm) {
        try {
            List<Booking> bookings;

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                bookings = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                        .searchBlockBookings(searchTerm);
            } else {
                // Convert string parameters to enums
                Booking.BookingStatus bookingStatus = null;
                Booking.PaymentStatus paymentStatusEnum = null;

                if (status != null) {
                    try {
                        bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                    }
                }

                if (paymentStatus != null) {
                    try {
                        paymentStatusEnum = Booking.PaymentStatus.valueOf(paymentStatus.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                    }
                }

                bookings = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                        .getBlockBookingsWithFilters(agencyId, bookingStatus, paymentStatusEnum, startDate, endDate);
            }

            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============ PAYMENT MANAGEMENT ============

    @PutMapping("/{id}/payment")
    public ResponseEntity<Booking> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> paymentUpdate) {
        try {
            String paymentStatusStr = (String) paymentUpdate.get("paymentStatus");
            Object paidAmountObj = paymentUpdate.get("paidAmount");

            if (paymentStatusStr == null) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }

            Booking.PaymentStatus paymentStatus;
            try {
                paymentStatus = Booking.PaymentStatus.valueOf(paymentStatusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }

            BigDecimal paidAmount = null;
            if (paidAmountObj != null) {
                if (paidAmountObj instanceof Number) {
                    paidAmount = new BigDecimal(paidAmountObj.toString());
                } else if (paidAmountObj instanceof String) {
                    try {
                        paidAmount = new BigDecimal((String) paidAmountObj);
                    } catch (NumberFormatException e) {
                        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                    }
                }
            }

            Booking updatedBooking = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .updatePaymentStatus(id, paymentStatus, paidAmount);
            return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/payments/pending")
    public ResponseEntity<List<Booking>> getPendingPayments() {
        try {
            List<Booking> pendingBookings = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .getPendingPayments();
            return new ResponseEntity<>(pendingBookings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payments/overdue")
    public ResponseEntity<List<Booking>> getOverdueBookings() {
        try {
            List<Booking> overdueBookings = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .getOverdueBookings();
            return new ResponseEntity<>(overdueBookings, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/agency/{agencyId}/outstanding")
    public ResponseEntity<Map<String, Object>> getTotalOutstandingAmount(@PathVariable Long agencyId) {
        try {
            BigDecimal outstandingAmount = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .getTotalOutstandingAmount(agencyId);

            Map<String, Object> response = Map.of(
                    "agencyId", agencyId,
                    "outstandingAmount", outstandingAmount);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============ ROOM AVAILABILITY ============

    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> checkRoomAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false, defaultValue = "0") Integer standardRooms,
            @RequestParam(required = false, defaultValue = "0") Integer deluxeRooms,
            @RequestParam(required = false, defaultValue = "0") Integer suiteRooms) {
        try {
            boolean available = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .checkRoomAvailability(checkIn, checkOut, standardRooms, deluxeRooms, suiteRooms);

            Map<String, Object> response = Map.of(
                    "available", available,
                    "checkIn", checkIn,
                    "checkOut", checkOut,
                    "requestedRooms", Map.of(
                            "standard", standardRooms,
                            "deluxe", deluxeRooms,
                            "suite", suiteRooms));

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/occupancy/{date}")
    public ResponseEntity<Map<String, Object>> getRoomOccupancyOnDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Integer totalOccupied = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .getTotalRoomsOccupiedOnDate(date);

            Map<String, Object> response = Map.of(
                    "date", date,
                    "totalOccupied", totalOccupied);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============ REPORTING AND ANALYTICS ============

    @GetMapping("/revenue/agency/{agencyId}")
    public ResponseEntity<Map<String, Object>> getTotalRevenueByAgency(@PathVariable Long agencyId) {
        try {
            BigDecimal revenue = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .getTotalRevenueByAgency(agencyId);
            Long bookingCount = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .countBlockBookingsByAgency(agencyId);
            Double avgDiscount = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .getAverageDiscountByAgency(agencyId);

            Map<String, Object> response = Map.of(
                    "agencyId", agencyId,
                    "totalRevenue", revenue,
                    "totalBookings", bookingCount,
                    "averageDiscount", avgDiscount);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/revenue/daterange")
    public ResponseEntity<Map<String, Object>> getTotalRevenueByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            BigDecimal revenue = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .getTotalRevenueByDateRange(startDate, endDate);

            Map<String, Object> response = Map.of(
                    "startDate", startDate,
                    "endDate", endDate,
                    "totalRevenue", revenue);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ============ PRICING CALCULATIONS ============

    @PostMapping("/calculate-pricing")
    public ResponseEntity<Booking> calculateBookingPricing(@RequestBody Booking booking) {
        try {
            Booking calculatedBooking = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .calculateBookingPricing(booking);
            return new ResponseEntity<>(calculatedBooking, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/calculate-advance")
    public ResponseEntity<Map<String, Object>> calculateAdvanceAmount(
            @RequestParam BigDecimal totalAmount,
            @RequestParam String paymentTerms) {
        try {
            Booking.PaymentTerms terms = Booking.PaymentTerms.valueOf(paymentTerms.toUpperCase());
            BigDecimal advanceAmount = ((com.realnest.service.impl.BookingServiceImpl) bookingService)
                    .calculateAdvanceAmount(totalAmount, terms);

            Map<String, Object> response = Map.of(
                    "totalAmount", totalAmount,
                    "paymentTerms", terms.getDisplayName(),
                    "advanceAmount", advanceAmount);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}