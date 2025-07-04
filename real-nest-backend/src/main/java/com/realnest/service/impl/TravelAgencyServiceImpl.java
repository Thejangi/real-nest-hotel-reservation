package com.realnest.service.impl;

import com.realnest.entity.Booking;
import com.realnest.entity.TravelAgency;
import com.realnest.repository.BookingRepository;
import com.realnest.repository.TravelAgencyRepository;
import com.realnest.service.TravelAgencyService;
import com.realnest.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TravelAgencyServiceImpl implements TravelAgencyService {

    @Autowired
    private TravelAgencyRepository travelAgencyRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingService bookingService;

    @Override
    public TravelAgency createAgency(TravelAgency agency) {
        // Check if email already exists
        if (agency.getEmail() != null && travelAgencyRepository.existsByEmail(agency.getEmail())) {
            throw new RuntimeException("Agency with this email already exists");
        }
        return travelAgencyRepository.save(agency);
    }

    @Override
    public List<TravelAgency> getAllAgencies() {
        return travelAgencyRepository.findAll();
    }

    @Override
    public TravelAgency getAgencyById(Long id) {
        return travelAgencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Travel Agency not found with id: " + id));
    }

    @Override
    public TravelAgency updateAgency(Long id, TravelAgency agencyDetails) {
        TravelAgency existingAgency = travelAgencyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Travel Agency not found with id: " + id));

        // Check if email is being changed and if the new email already exists
        if (agencyDetails.getEmail() != null &&
                !agencyDetails.getEmail().equals(existingAgency.getEmail()) &&
                travelAgencyRepository.existsByEmail(agencyDetails.getEmail())) {
            throw new RuntimeException("Agency with this email already exists");
        }

        // Update fields
        if (agencyDetails.getName() != null) {
            existingAgency.setName(agencyDetails.getName());
        }
        if (agencyDetails.getEmail() != null) {
            existingAgency.setEmail(agencyDetails.getEmail());
        }
        if (agencyDetails.getPhone() != null) {
            existingAgency.setPhone(agencyDetails.getPhone());
        }
        if (agencyDetails.getAddress() != null) {
            existingAgency.setAddress(agencyDetails.getAddress());
        }

        // Update boolean and numeric fields
        existingAgency.setPrepaymentRequired(agencyDetails.isPrepaymentRequired());
        existingAgency.setDiscountPercentage(agencyDetails.getDiscountPercentage());

        return travelAgencyRepository.save(existingAgency);
    }

    @Override
    public void deleteAgency(Long id) {
        if (!travelAgencyRepository.existsById(id)) {
            throw new RuntimeException("Travel Agency not found with id: " + id);
        }

        // Check if agency has active bookings
        List<Booking> activeBookings = bookingRepository.findByTravelAgencyId(id)
                .stream()
                .filter(booking -> booking.getBookingStatus() == Booking.BookingStatus.CONFIRMED ||
                        booking.getBookingStatus() == Booking.BookingStatus.PENDING)
                .collect(Collectors.toList());

        if (!activeBookings.isEmpty()) {
            throw new RuntimeException(
                    "Cannot delete agency with active bookings. Please cancel or complete all bookings first.");
        }

        travelAgencyRepository.deleteById(id);
    }

    // Enhanced bulk booking logic with proper enum handling and comprehensive
    // pricing
    @Override
    public List<Booking> createBulkBooking(Long agencyId, List<Booking> bookings) {
        TravelAgency agency = travelAgencyRepository.findById(agencyId)
                .orElseThrow(() -> new RuntimeException("Travel Agency not found with id: " + agencyId));

        // Validate that all bookings qualify as block bookings (3+ rooms)
        for (Booking booking : bookings) {
            if (booking.getNumberOfRooms() == null || booking.getNumberOfRooms() < 3) {
                throw new RuntimeException("All bookings must have at least 3 rooms to qualify as block bookings");
            }
        }

        return bookings.stream()
                .map(booking -> processIndividualBlockBooking(booking, agency))
                .map(bookingRepository::save)
                .collect(Collectors.toList());
    }

    /**
     * Process a single booking with agency rules and pricing calculations
     */
    private Booking processIndividualBlockBooking(Booking booking, TravelAgency agency) {
        // Set the travel agency ID
        booking.setTravelAgencyId(agency.getId());

        // Set agency discount if not already specified or if agency discount is higher
        BigDecimal agencyDiscount = BigDecimal.valueOf(agency.getDiscountPercentage());
        if (booking.getDiscountPercentage() == null ||
                booking.getDiscountPercentage().compareTo(agencyDiscount) < 0) {
            booking.setDiscountPercentage(agencyDiscount);
        }

        // Calculate comprehensive pricing using the booking service
        booking = ((BookingServiceImpl) bookingService).calculateBookingPricing(booking);

        // Enforce prepayment rule
        if (agency.isPrepaymentRequired()) {
            booking.setPaymentStatus(Booking.PaymentStatus.PAID);
            booking.setPaymentTerms(Booking.PaymentTerms.FULL_ADVANCE);
            booking.setAdvancePaidAmount(booking.getTotalAmount());
        } else {
            booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
            if (booking.getPaymentTerms() == null) {
                booking.setPaymentTerms(Booking.PaymentTerms.FIFTY_ADVANCE);
            }
        }

        // Set booking status to confirmed for agency bookings
        booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);

        // Generate block reference if not provided
        if (booking.getBlockReference() == null || booking.getBlockReference().isEmpty()) {
            booking.setBlockReference(generateBlockReference(agency));
        }

        return booking;
    }

    /**
     * Generate a unique block reference for the agency
     */
    private String generateBlockReference(TravelAgency agency) {
        String agencyCode = agency.getName().replaceAll("[^A-Za-z]", "").toUpperCase();
        if (agencyCode.length() > 3) {
            agencyCode = agencyCode.substring(0, 3);
        } else if (agencyCode.length() < 3) {
            agencyCode = String.format("%-3s", agencyCode).replace(' ', 'X');
        }

        return agencyCode + "-BLK-" + System.currentTimeMillis() % 100000;
    }

    // ============ ADDITIONAL AGENCY-SPECIFIC BOOKING METHODS ============

    /**
     * Get all block bookings for a specific agency
     */
    public List<Booking> getAgencyBlockBookings(Long agencyId) {
        return bookingRepository.findBlockBookingsByAgency(agencyId);
    }

    /**
     * Get agency booking statistics
     */
    public AgencyBookingStats getAgencyBookingStats(Long agencyId) {
        TravelAgency agency = getAgencyById(agencyId);
        List<Booking> agencyBookings = bookingRepository.findByTravelAgencyId(agencyId);
        List<Booking> blockBookings = bookingRepository.findBlockBookingsByAgency(agencyId);

        // Calculate statistics
        int totalBookings = agencyBookings.size();
        int totalBlockBookings = blockBookings.size();

        BigDecimal totalRevenue = agencyBookings.stream()
                .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutstanding = agencyBookings.stream()
                .map(Booking::getRemainingAmount)
                .filter(amount -> amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long confirmedBookings = agencyBookings.stream()
                .filter(b -> b.getBookingStatus() == Booking.BookingStatus.CONFIRMED)
                .count();

        long pendingBookings = agencyBookings.stream()
                .filter(b -> b.getBookingStatus() == Booking.BookingStatus.PENDING)
                .count();

        Double averageDiscount = agencyBookings.stream()
                .filter(b -> b.getDiscountPercentage() != null)
                .mapToDouble(b -> b.getDiscountPercentage().doubleValue())
                .average()
                .orElse(0.0);

        return new AgencyBookingStats(
                agency.getId(),
                agency.getName(),
                totalBookings,
                totalBlockBookings,
                totalRevenue,
                totalOutstanding,
                (int) confirmedBookings,
                (int) pendingBookings,
                averageDiscount);
    }

    /**
     * Update agency payment terms for future bookings
     */
    public TravelAgency updateAgencyPaymentTerms(Long agencyId, boolean prepaymentRequired, double discountPercentage) {
        TravelAgency agency = getAgencyById(agencyId);
        agency.setPrepaymentRequired(prepaymentRequired);
        agency.setDiscountPercentage(discountPercentage);
        return travelAgencyRepository.save(agency);
    }

    /**
     * Bulk update booking status for agency bookings
     */
    @Transactional
    public List<Booking> updateAgencyBookingsStatus(Long agencyId, Booking.BookingStatus newStatus,
            List<Long> bookingIds) {
        // Verify agency exists
        getAgencyById(agencyId);

        List<Booking> bookingsToUpdate = bookingRepository.findAllById(bookingIds);

        // Verify all bookings belong to the agency
        bookingsToUpdate.forEach(booking -> {
            if (!booking.getTravelAgencyId().equals(agencyId)) {
                throw new RuntimeException("Booking " + booking.getId() + " does not belong to agency " + agencyId);
            }
            booking.setBookingStatus(newStatus);
        });

        return bookingRepository.saveAll(bookingsToUpdate);
    }

    /**
     * Apply agency-wide discount update to pending bookings
     */
    @Transactional
    public List<Booking> applyAgencyDiscountUpdate(Long agencyId, BigDecimal newDiscountPercentage) {
        TravelAgency agency = getAgencyById(agencyId);
        agency.setDiscountPercentage(newDiscountPercentage.doubleValue());
        travelAgencyRepository.save(agency);

        // Update pending bookings with new discount
        List<Booking> pendingBookings = bookingRepository.findByTravelAgencyId(agencyId)
                .stream()
                .filter(b -> b.getBookingStatus() == Booking.BookingStatus.PENDING)
                .collect(Collectors.toList());

        return pendingBookings.stream()
                .map(booking -> {
                    booking.setDiscountPercentage(newDiscountPercentage);
                    return ((BookingServiceImpl) bookingService).calculateBookingPricing(booking);
                })
                .map(bookingRepository::save)
                .collect(Collectors.toList());
    }

    // ============ VALIDATION METHODS ============

    /**
     * Validate agency can create block booking
     */
    public boolean canCreateBlockBooking(Long agencyId, int totalRooms) {
        TravelAgency agency = getAgencyById(agencyId);

        // Basic validation - at least 3 rooms for block booking
        if (totalRooms < 3) {
            return false;
        }

        // Additional business rules can be added here
        // e.g., credit limit checks, outstanding payment checks, etc.

        return true;
    }

    /**
     * Get agency credit status
     */
    public AgencyCreditStatus getAgencyCreditStatus(Long agencyId) {
        BigDecimal outstandingAmount = ((BookingServiceImpl) bookingService).getTotalOutstandingAmount(agencyId);
        Long totalBookings = bookingRepository.countBlockBookingsByAgency(agencyId);

        // Simple credit status based on outstanding amount
        String status = "GOOD";
        if (outstandingAmount.compareTo(new BigDecimal("10000")) > 0) {
            status = "WARNING";
        }
        if (outstandingAmount.compareTo(new BigDecimal("25000")) > 0) {
            status = "POOR";
        }

        return new AgencyCreditStatus(agencyId, outstandingAmount, totalBookings, status);
    }

    // ============ INNER CLASSES FOR RESPONSE DTOs ============

    public static class AgencyBookingStats {
        private Long agencyId;
        private String agencyName;
        private int totalBookings;
        private int totalBlockBookings;
        private BigDecimal totalRevenue;
        private BigDecimal totalOutstanding;
        private int confirmedBookings;
        private int pendingBookings;
        private Double averageDiscount;

        public AgencyBookingStats(Long agencyId, String agencyName, int totalBookings,
                int totalBlockBookings, BigDecimal totalRevenue,
                BigDecimal totalOutstanding, int confirmedBookings,
                int pendingBookings, Double averageDiscount) {
            this.agencyId = agencyId;
            this.agencyName = agencyName;
            this.totalBookings = totalBookings;
            this.totalBlockBookings = totalBlockBookings;
            this.totalRevenue = totalRevenue;
            this.totalOutstanding = totalOutstanding;
            this.confirmedBookings = confirmedBookings;
            this.pendingBookings = pendingBookings;
            this.averageDiscount = averageDiscount;
        }

        // Getters
        public Long getAgencyId() {
            return agencyId;
        }

        public String getAgencyName() {
            return agencyName;
        }

        public int getTotalBookings() {
            return totalBookings;
        }

        public int getTotalBlockBookings() {
            return totalBlockBookings;
        }

        public BigDecimal getTotalRevenue() {
            return totalRevenue;
        }

        public BigDecimal getTotalOutstanding() {
            return totalOutstanding;
        }

        public int getConfirmedBookings() {
            return confirmedBookings;
        }

        public int getPendingBookings() {
            return pendingBookings;
        }

        public Double getAverageDiscount() {
            return averageDiscount;
        }
    }

    public static class AgencyCreditStatus {
        private Long agencyId;
        private BigDecimal outstandingAmount;
        private Long totalBookings;
        private String status;

        public AgencyCreditStatus(Long agencyId, BigDecimal outstandingAmount, Long totalBookings, String status) {
            this.agencyId = agencyId;
            this.outstandingAmount = outstandingAmount;
            this.totalBookings = totalBookings;
            this.status = status;
        }

        // Getters
        public Long getAgencyId() {
            return agencyId;
        }

        public BigDecimal getOutstandingAmount() {
            return outstandingAmount;
        }

        public Long getTotalBookings() {
            return totalBookings;
        }

        public String getStatus() {
            return status;
        }
    }
}