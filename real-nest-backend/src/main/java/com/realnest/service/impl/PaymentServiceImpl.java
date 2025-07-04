package com.realnest.service.impl;

import com.realnest.entity.Payment;
import com.realnest.repository.PaymentRepository;
import com.realnest.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Payment updatePayment(Long id, Payment payment) {
        Payment existing = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        
        existing.setAmount(payment.getAmount());
        existing.setPaymentMethod(payment.getPaymentMethod());
        existing.setPaymentStatus(payment.getPaymentStatus());
        existing.setPaymentDate(payment.getPaymentDate());
        existing.setBookingId(payment.getBookingId());
        
        return paymentRepository.save(existing);
    }

    @Override
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public List<Payment> getPaymentsByBookingId(Long bookingId) {
        // Use stream to filter since custom method doesn't exist yet
        return paymentRepository.findAll().stream()
                .filter(payment -> bookingId.equals(payment.getBookingId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> getPaymentsByStatus(String status) {
        // Use stream to filter since custom method doesn't exist yet
        return paymentRepository.findAll().stream()
                .filter(payment -> status.equals(payment.getPaymentStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        // Use stream to filter since custom method doesn't exist yet
        return paymentRepository.findAll().stream()
                .filter(payment -> {
                    LocalDate paymentDate = payment.getPaymentDate();
                    return paymentDate != null && 
                           !paymentDate.isBefore(startDate) && 
                           !paymentDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }
}