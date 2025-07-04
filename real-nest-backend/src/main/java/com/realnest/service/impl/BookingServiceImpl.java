package com.realnest.service.impl;

import com.realnest.entity.Booking;
import com.realnest.entity.TravelAgency;
import com.realnest.dto.BookingStats;
import com.realnest.repository.BookingRepository;
import com.realnest.repository.TravelAgencyRepository;
import com.realnest.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TravelAgencyRepository travelAgencyRepository;

    // Room rates (should ideally come from configuration)
    private static final BigDecimal STANDARD_ROOM_RATE = new BigDecimal("120.00");
    private static final BigDecimal DELUXE_ROOM_RATE = new BigDecimal("180.00");
    private static final BigDecimal SUITE_ROOM_RATE = new BigDecimal("300.00");

    // ============ EXISTING METHODS (Backward Compatibility) ============

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public List<Booking> getBookingsByStatus(String status) {
        // Convert string to enum for backward compatibility
        try {
            Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
            return bookingRepository.findByBookingStatus(bookingStatus);
        } catch (IllegalArgumentException e) {
            // Fallback for old string-based queries
            return getAllBookings().stream()
                    .filter(b -> status.equalsIgnoreCase(b.getBookingStatus().name()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Booking not found with id: " + id));
    }

    @Override
    public Booking updateBookingStatus(Long id, String status) {
        Booking booking = getBookingById(id);
        try {
            Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
            booking.setBookingStatus(bookingStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid booking status: " + status);
        }
        return bookingRepository.save(booking);
    }

    @Override
    public BookingStats getBookingStatistics() {
        // Use new repository methods for better performance
        Long activeBlocks = bookingRepository.countActiveBlockBookings();

        List<Booking> allBookings = getAllBookings();
        int totalRooms = allBookings.stream()
                .mapToInt(b -> b.getNumberOfRooms() != null ? b.getNumberOfRooms() : 0)
                .sum();

        BigDecimal totalRevenue = allBookings.stream()
                .map(booking -> booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long confirmedBookings = allBookings.stream()
                .filter(b -> b.getBookingStatus() == Booking.BookingStatus.CONFIRMED)
                .count();

        long pendingBookings = allBookings.stream()
                .filter(b -> b.getBookingStatus() == Booking.BookingStatus.PENDING)
                .count();

        // Calculate average discount
        Double avgDiscount = allBookings.stream()
                .filter(b -> b.getDiscountPercentage() != null)
                .mapToDouble(b -> b.getDiscountPercentage().doubleValue())
                .average()
                .orElse(0.0);

        return new BookingStats(
                activeBlocks.intValue(),
                totalRooms,
                totalRevenue,
                avgDiscount,
                (int) confirmedBookings,
                (int) pendingBookings);
    }

    // ============ NEW BLOCK BOOKING METHODS ============

    @Override
    public Booking createBlockBooking(Booking booking) {
        // Validate minimum rooms for block booking
        if (booking.getNumberOfRooms() == null || booking.getNumberOfRooms() < 3) {
            throw new RuntimeException("Block booking requires minimum 3 rooms");
        }

        // Apply agency discount and payment rules if agency is specified
        if (booking.getTravelAgencyId() != null) {
            TravelAgency agency = travelAgencyRepository.findById(booking.getTravelAgencyId())
                    .orElseThrow(() -> new RuntimeException("Travel Agency not found"));

            // Set discount percentage from agency
            if (booking.getDiscountPercentage() == null ||
                    booking.getDiscountPercentage().compareTo(BigDecimal.valueOf(agency.getDiscountPercentage())) > 0) {
                booking.setDiscountPercentage(BigDecimal.valueOf(agency.getDiscountPercentage()));
            }

            // Enforce prepayment rule
            if (agency.isPrepaymentRequired()) {
                booking.setPaymentTerms(Booking.PaymentTerms.FULL_ADVANCE);
            }
        }

        // Calculate pricing
        booking = calculateBookingPricing(booking);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking updateBlockBooking(Long id, Booking bookingDetails) {
        Booking existingBooking = getBookingById(id);

        // Update fields
        if (bookingDetails.getCheckInDate() != null) {
            existingBooking.setCheckInDate(bookingDetails.getCheckInDate());
        }
        if (bookingDetails.getCheckOutDate() != null) {
            existingBooking.setCheckOutDate(bookingDetails.getCheckOutDate());
        }
        if (bookingDetails.getNumberOfRooms() != null) {
            if (bookingDetails.getNumberOfRooms() < 3) {
                throw new RuntimeException("Block booking requires minimum 3 rooms");
            }
            existingBooking.setNumberOfRooms(bookingDetails.getNumberOfRooms());
        }
        if (bookingDetails.getStandardRooms() != null) {
            existingBooking.setStandardRooms(bookingDetails.getStandardRooms());
        }
        if (bookingDetails.getDeluxeRooms() != null) {
            existingBooking.setDeluxeRooms(bookingDetails.getDeluxeRooms());
        }
        if (bookingDetails.getSuiteRooms() != null) {
            existingBooking.setSuiteRooms(bookingDetails.getSuiteRooms());
        }
        if (bookingDetails.getGroupSize() != null) {
            existingBooking.setGroupSize(bookingDetails.getGroupSize());
        }
        if (bookingDetails.getMealPlan() != null) {
            existingBooking.setMealPlan(bookingDetails.getMealPlan());
        }
        if (bookingDetails.getSpecialRequirements() != null) {
            existingBooking.setSpecialRequirements(bookingDetails.getSpecialRequirements());
        }
        if (bookingDetails.getBookingStatus() != null) {
            existingBooking.setBookingStatus(bookingDetails.getBookingStatus());
        }
        if (bookingDetails.getPaymentStatus() != null) {
            existingBooking.setPaymentStatus(bookingDetails.getPaymentStatus());
        }
        if (bookingDetails.getPaymentTerms() != null) {
            existingBooking.setPaymentTerms(bookingDetails.getPaymentTerms());
        }

        // Recalculate pricing
        existingBooking = calculateBookingPricing(existingBooking);

        return bookingRepository.save(existingBooking);
    }

    @Override
    public void deleteBlockBooking(Long id) {
        Booking booking = getBookingById(id);
        if (!booking.isBlockBooking()) {
            throw new RuntimeException("Not a block booking");
        }
        bookingRepository.deleteById(id);
    }

    @Override
    public List<Booking> getAllBlockBookings() {
        return bookingRepository.findAllBlockBookings();
    }

    @Override
    public List<Booking> getBlockBookingsByAgency(Long agencyId) {
        return bookingRepository.findBlockBookingsByAgency(agencyId);
    }

    @Override
    public List<Booking> getBlockBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.findBlockBookingsByStatus(status);
    }

    @Override
    public Booking getBlockBookingByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new RuntimeException("Block booking not found with reference: " + bookingReference));
    }

    @Override
    public List<Booking> getBlockBookingsWithFilters(Long agencyId, Booking.BookingStatus status,
            Booking.PaymentStatus paymentStatus,
            LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findBlockBookingsWithFilters(agencyId, status, paymentStatus, startDate, endDate);
    }

    @Override
    public List<Booking> searchBlockBookings(String searchTerm) {
        return bookingRepository.searchBlockBookings(searchTerm);
    }

    // ============ PAYMENT MANAGEMENT ============

    @Override
    public Booking updatePaymentStatus(Long id, Booking.PaymentStatus paymentStatus, BigDecimal paidAmount) {
        Booking booking = getBookingById(id);
        booking.setPaymentStatus(paymentStatus);

        if (paidAmount != null) {
            BigDecimal currentPaid = booking.getAdvancePaidAmount() != null ? booking.getAdvancePaidAmount()
                    : BigDecimal.ZERO;
            booking.setAdvancePaidAmount(currentPaid.add(paidAmount));

            // Update payment status based on amount paid
            if (booking.isFullyPaid()) {
                booking.setPaymentStatus(Booking.PaymentStatus.PAID);
            } else if (booking.getAdvancePaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                booking.setPaymentStatus(Booking.PaymentStatus.PARTIAL);
            }
        }

        return bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getPendingPayments() {
        return bookingRepository.findPendingPaymentsForToday(LocalDate.now());
    }

    @Override
    public List<Booking> getOverdueBookings() {
        return bookingRepository.findOverdueBookings(LocalDate.now());
    }

    @Override
    public BigDecimal getTotalOutstandingAmount(Long agencyId) {
        List<Booking> agencyBookings = getBlockBookingsByAgency(agencyId);
        return agencyBookings.stream()
                .map(Booking::getRemainingAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ============ ROOM AVAILABILITY ============

    @Override
    public boolean checkRoomAvailability(LocalDate checkIn, LocalDate checkOut,
            Integer standardRooms, Integer deluxeRooms, Integer suiteRooms) {
        // This is a simplified check - in a real system, you'd have a room inventory
        // For now, just check if the total requested rooms don't exceed a reasonable
        // limit
        int totalRequested = (standardRooms != null ? standardRooms : 0) +
                (deluxeRooms != null ? deluxeRooms : 0) +
                (suiteRooms != null ? suiteRooms : 0);

        // Assume hotel has 50 standard, 30 deluxe, 20 suite rooms
        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            Integer occupiedStandard = bookingRepository.getStandardRoomsOccupiedOnDate(date);
            Integer occupiedDeluxe = bookingRepository.getDeluxeRoomsOccupiedOnDate(date);
            Integer occupiedSuite = bookingRepository.getSuiteRoomsOccupiedOnDate(date);

            if ((occupiedStandard != null ? occupiedStandard : 0) + (standardRooms != null ? standardRooms : 0) > 50 ||
                    (occupiedDeluxe != null ? occupiedDeluxe : 0) + (deluxeRooms != null ? deluxeRooms : 0) > 30 ||
                    (occupiedSuite != null ? occupiedSuite : 0) + (suiteRooms != null ? suiteRooms : 0) > 20) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Integer getTotalRoomsOccupiedOnDate(LocalDate date) {
        Integer occupied = bookingRepository.getTotalRoomsOccupiedOnDate(date);
        return occupied != null ? occupied : 0;
    }

    // ============ STATISTICS AND REPORTING ============

    @Override
    public BookingStats getBlockBookingStatistics() {
        return getBookingStatistics(); // Reuse existing method
    }

    @Override
    public BigDecimal getTotalRevenueByAgency(Long agencyId) {
        BigDecimal revenue = bookingRepository.getTotalRevenueByAgency(agencyId);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        BigDecimal revenue = bookingRepository.getTotalRevenueByDateRange(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Override
    public Long countBlockBookingsByAgency(Long agencyId) {
        return bookingRepository.countBlockBookingsByAgency(agencyId);
    }

    @Override
    public Double getAverageDiscountByAgency(Long agencyId) {
        Double avgDiscount = bookingRepository.getAverageDiscountByAgency(agencyId);
        return avgDiscount != null ? avgDiscount : 0.0;
    }

    // ============ BULK OPERATIONS ============

    @Override
    public List<Booking> createBulkBlockBookings(Long agencyId, List<Booking> bookings) {
        TravelAgency agency = travelAgencyRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Travel Agency not found"));

        return bookings.stream()
                .map(booking -> {
                    booking.setTravelAgencyId(agencyId);

                    // Apply agency discount
                    if (booking.getDiscountPercentage() == null) {
                        booking.setDiscountPercentage(BigDecimal.valueOf(agency.getDiscountPercentage()));
                    }

                    // Enforce prepayment rule
                    if (agency.isPrepaymentRequired()) {
                        booking.setPaymentStatus(Booking.PaymentStatus.PAID);
                        booking.setPaymentTerms(Booking.PaymentTerms.FULL_ADVANCE);
                    } else {
                        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
                    }

                    booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);

                    return calculateBookingPricing(booking);
                })
                .map(bookingRepository::save)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> getBookingsByAgencyAndDateRange(Long agencyId, LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findBookingsByAgencyAndDateRange(agencyId, startDate, endDate);
    }

    // ============ PRICING CALCULATIONS ============

    @Override
    public Booking calculateBookingPricing(Booking booking) {
        if (booking.getNumberOfNights() == null || booking.getNumberOfNights() <= 0) {
            return booking;
        }

        // Calculate subtotal based on room distribution
        BigDecimal subtotal = BigDecimal.ZERO;

        if (booking.getStandardRooms() != null && booking.getStandardRooms() > 0) {
            subtotal = subtotal.add(STANDARD_ROOM_RATE
                    .multiply(BigDecimal.valueOf(booking.getStandardRooms()))
                    .multiply(BigDecimal.valueOf(booking.getNumberOfNights())));
        }

        if (booking.getDeluxeRooms() != null && booking.getDeluxeRooms() > 0) {
            subtotal = subtotal.add(DELUXE_ROOM_RATE
                    .multiply(BigDecimal.valueOf(booking.getDeluxeRooms()))
                    .multiply(BigDecimal.valueOf(booking.getNumberOfNights())));
        }

        if (booking.getSuiteRooms() != null && booking.getSuiteRooms() > 0) {
            subtotal = subtotal.add(SUITE_ROOM_RATE
                    .multiply(BigDecimal.valueOf(booking.getSuiteRooms()))
                    .multiply(BigDecimal.valueOf(booking.getNumberOfNights())));
        }

        booking.setSubtotalAmount(subtotal);

        // Calculate discount amount
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (booking.getDiscountPercentage() != null && booking.getDiscountPercentage().compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = subtotal.multiply(booking.getDiscountPercentage())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        booking.setDiscountAmount(discountAmount);

        // Calculate total amount
        BigDecimal totalAmount = subtotal.subtract(discountAmount);
        booking.setTotalAmount(totalAmount);

        // Calculate advance amount based on payment terms
        if (booking.getPaymentTerms() != null) {
            BigDecimal advanceAmount = calculateAdvanceAmount(totalAmount, booking.getPaymentTerms());
            booking.setAdvanceAmount(advanceAmount);
        }

        return booking;
    }

    @Override
    public BigDecimal calculateAdvanceAmount(BigDecimal totalAmount, Booking.PaymentTerms paymentTerms) {
        if (totalAmount == null || paymentTerms == null) {
            return BigDecimal.ZERO;
        }

        switch (paymentTerms) {
            case FULL_ADVANCE:
                return totalAmount;
            case FIFTY_ADVANCE:
                return totalAmount.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
            case THIRTY_ADVANCE:
                return totalAmount.multiply(BigDecimal.valueOf(0.3)).setScale(2, RoundingMode.HALF_UP);
            default:
                return totalAmount;
        }
    }
}