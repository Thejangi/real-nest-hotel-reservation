package com.realnest.service;

import com.realnest.entity.Payment;

import java.time.LocalDate;
import java.util.List;

public interface PaymentService {

    Payment createPayment(Payment payment);
    
    Payment updatePayment(Long id, Payment payment);
    
    void deletePayment(Long id);
    
    Payment getPaymentById(Long id);
    
    List<Payment> getAllPayments();
    
    // Additional methods (optional)
    List<Payment> getPaymentsByBookingId(Long bookingId);
    
    List<Payment> getPaymentsByStatus(String status);
    
    List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate);
}