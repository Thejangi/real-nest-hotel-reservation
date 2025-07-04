package com.realnest.repository;

import com.realnest.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);
}
