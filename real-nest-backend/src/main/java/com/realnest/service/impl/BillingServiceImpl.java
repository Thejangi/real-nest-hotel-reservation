package com.realnest.service.impl;

import com.realnest.entity.Invoice;
import com.realnest.entity.Booking;
import com.realnest.dto.BillingSummary;
import com.realnest.repository.InvoiceRepository;
import com.realnest.repository.BookingRepository;
import com.realnest.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BillingServiceImpl implements BillingService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public BillingSummary getBillingSummary() {
        List<Invoice> allInvoices = invoiceRepository.findAll();

        BigDecimal totalOutstanding = calculateOutstanding(allInvoices);
        BigDecimal totalPaid = calculatePaid(allInvoices);
        BigDecimal overdue = calculateOverdue(allInvoices);
        List<Invoice> recentInvoices = getRecentInvoices();

        return new BillingSummary(totalOutstanding, totalPaid, overdue, recentInvoices);
    }

    @Override
    public List<Invoice> getInvoices(String status) {
        if (status != null) {
            return invoiceRepository.findByStatus(status);
        }
        return invoiceRepository.findAll();
    }

    @Override
    public Invoice generateInvoice(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new RuntimeException("Booking not found with id: " + bookingId));

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV" + System.currentTimeMillis())
                .bookingId(bookingId)
                .agencyId(booking.getTravelAgencyId()) // Use the foreign key field
                .amount(booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .status("PENDING")
                .build();

        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice updateInvoiceStatus(Long invoiceId, String status) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(
                () -> new RuntimeException("Invoice not found with id: " + invoiceId));

        invoice.setStatus(status);
        if ("PAID".equals(status)) {
            invoice.setPaymentDate(LocalDate.now());
        }

        return invoiceRepository.save(invoice);
    }

    // Helper methods
    private BigDecimal calculateOutstanding(List<Invoice> invoices) {
        return invoices.stream()
                .filter(inv -> "PENDING".equals(inv.getStatus()) || "OVERDUE".equals(inv.getStatus()))
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculatePaid(List<Invoice> invoices) {
        return invoices.stream()
                .filter(inv -> "PAID".equals(inv.getStatus()))
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateOverdue(List<Invoice> invoices) {
        LocalDate today = LocalDate.now();
        return invoices.stream()
                .filter(inv -> "PENDING".equals(inv.getStatus()) && inv.getDueDate().isBefore(today))
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Invoice> getRecentInvoices() {
        return invoiceRepository.findAll().stream()
                .sorted((a, b) -> b.getIssueDate().compareTo(a.getIssueDate()))
                .limit(10)
                .toList();
    }
}