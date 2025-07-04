package com.realnest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Date fields - match your actual database columns
    @Column(name = "check_in_date")
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    @Column(name = "arrival_date")
    private LocalDate arrivalDate;

    @Column(name = "departure_date")
    private LocalDate departureDate;

    // Guest information
    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "email")
    private String email;

    // Booking details
    @Column(name = "booking_status")
    private String bookingStatus;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "total_amount", precision = 38, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "number_of_guests")
    private Integer numberOfGuests;

    @Column(name = "occupants")
    private Integer occupants;

    // Room information
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_type")
    private String roomType;

    @Column(name = "card_number")
    private String cardNumber;

    // Travel agency relationship
    @Column(name = "travel_agency_id")
    private Long travelAgencyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_agency_id", insertable = false, updatable = false)
    private TravelAgency travelAgency;

    // Add this getter for compatibility with our new services
    public Integer getNumberOfRooms() {
        // Since your database doesn't have number_of_rooms,
        // we'll return 1 for now (each booking = 1 room)
        return 1;
    }
}