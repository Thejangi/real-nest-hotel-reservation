package com.realnest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", unique = true)
    private String invoiceNumber;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "agency_id")
    private Long agencyId;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "status")
    private String status;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "notes")
    private String notes;
}