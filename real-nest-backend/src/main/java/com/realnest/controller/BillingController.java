package com.realnest.controller;

import com.realnest.entity.Invoice;
import com.realnest.dto.BillingSummary;
import com.realnest.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class BillingController {

    @Autowired
    private BillingService billingService;

    // Get billing summary
    @GetMapping("/summary")
    public BillingSummary getBillingSummary() {
        return billingService.getBillingSummary();
    }

    // Get all invoices
    @GetMapping("/invoices")
    public List<Invoice> getAllInvoices(@RequestParam(required = false) String status) {
        return billingService.getInvoices(status);
    }

    // Generate invoice for booking
    @PostMapping("/generate/{bookingId}")
    public Invoice generateInvoice(@PathVariable Long bookingId) {
        return billingService.generateInvoice(bookingId);
    }

    // Update invoice status
    @PutMapping("/invoices/{invoiceId}/status")
    public Invoice updateInvoiceStatus(@PathVariable Long invoiceId, @RequestParam String status) {
        return billingService.updateInvoiceStatus(invoiceId, status);
    }
}