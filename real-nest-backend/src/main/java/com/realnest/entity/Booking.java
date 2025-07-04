package com.realnest.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_reference", unique = true)
    private String bookingReference;

    @Column(name = "travel_agency_id")
    private Long travelAgencyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_agency_id", insertable = false, updatable = false)
    @JsonIgnore
    private TravelAgency travelAgency;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "number_of_nights")
    private Integer numberOfNights;

    @Column(name = "number_of_rooms", nullable = false)
    private Integer numberOfRooms;

    // Room distribution
    @Column(name = "standard_rooms")
    @Builder.Default
    private Integer standardRooms = 0;

    @Column(name = "deluxe_rooms")
    @Builder.Default
    private Integer deluxeRooms = 0;

    @Column(name = "suite_rooms")
    @Builder.Default
    private Integer suiteRooms = 0;

    // Pricing
    @Column(name = "subtotal_amount", precision = 10, scale = 2)
    private BigDecimal subtotalAmount;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Payment information
    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_terms")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentTerms paymentTerms = PaymentTerms.FULL_ADVANCE;

    @Column(name = "advance_amount", precision = 10, scale = 2)
    private BigDecimal advanceAmount;

    @Column(name = "advance_paid_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal advancePaidAmount = BigDecimal.ZERO;

    // Booking details
    @Column(name = "booking_status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    @Column(name = "group_size")
    private Integer groupSize;

    @Column(name = "meal_plan")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MealPlan mealPlan = MealPlan.ROOM_ONLY;

    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;

    @Column(name = "block_reference")
    private String blockReference;

    // Audit fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.bookingReference == null) {
            this.bookingReference = generateBookingReference();
        }
        calculateNights();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateNights();
    }

    private String generateBookingReference() {
        return "BLK" + System.currentTimeMillis();
    }

    private void calculateNights() {
        if (this.checkInDate != null && this.checkOutDate != null) {
            this.numberOfNights = (int) this.checkInDate.until(this.checkOutDate).getDays();
        }
    }

    // Enums
    public enum PaymentStatus {
        PENDING("Pending"),
        PAID("Paid"),
        PARTIAL("Partial"),
        OVERDUE("Overdue"),
        REFUNDED("Refunded");

        private final String displayName;

        PaymentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum BookingStatus {
        PENDING("Pending"),
        CONFIRMED("Confirmed"),
        CANCELLED("Cancelled"),
        COMPLETED("Completed"),
        NO_SHOW("No Show");

        private final String displayName;

        BookingStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PaymentTerms {
        FULL_ADVANCE("Full Payment in Advance"),
        FIFTY_ADVANCE("50% Advance + 50% on Arrival"),
        THIRTY_ADVANCE("30% Advance + 70% on Arrival");

        private final String displayName;

        PaymentTerms(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum MealPlan {
        ROOM_ONLY("Room Only"),
        BREAKFAST("Breakfast Included"),
        HALF_BOARD("Half Board"),
        FULL_BOARD("Full Board"),
        ALL_INCLUSIVE("All Inclusive");

        private final String displayName;

        MealPlan(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Helper methods
    public boolean isBlockBooking() {
        return this.numberOfRooms != null && this.numberOfRooms >= 3;
    }

    public BigDecimal getRemainingAmount() {
        if (this.totalAmount != null && this.advancePaidAmount != null) {
            return this.totalAmount.subtract(this.advancePaidAmount);
        }
        return this.totalAmount;
    }

    public boolean isFullyPaid() {
        return this.paymentStatus == PaymentStatus.PAID ||
                (this.totalAmount != null && this.advancePaidAmount != null &&
                        this.advancePaidAmount.compareTo(this.totalAmount) >= 0);
    }
}