package com.realnest.service;

import com.realnest.entity.Invoice;
import com.realnest.dto.BillingSummary;
import java.util.List;

public interface BillingService {
    BillingSummary getBillingSummary();

    List<Invoice> getInvoices(String status);

    Invoice generateInvoice(Long bookingId);

    Invoice updateInvoiceStatus(Long invoiceId, String status);
}