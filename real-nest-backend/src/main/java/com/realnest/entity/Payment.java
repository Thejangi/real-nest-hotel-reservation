package com.realnest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    // Simple Long field for booking reference (easier to manage)
    @Column(name = "booking_id")
    private Long bookingId;

    // Optional additional fields
    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "description")
    private String description;

    @PrePersist
    protected void onCreate() {
        if (paymentDate == null) {
            paymentDate = LocalDate.now();
        }
        if (paymentStatus == null) {
            paymentStatus = "PENDING";
        }
    }
}