package com.realnest.dto;

import com.realnest.entity.Invoice;
import java.math.BigDecimal;
import java.util.List;

public class BillingSummary {
    private BigDecimal totalOutstanding;
    private BigDecimal totalPaid;
    private BigDecimal overdue;
    private List<Invoice> recentInvoices;

    // Default constructor
    public BillingSummary() {
    }

    // Constructor with all fields
    public BillingSummary(BigDecimal totalOutstanding, BigDecimal totalPaid,
            BigDecimal overdue, List<Invoice> recentInvoices) {
        this.totalOutstanding = totalOutstanding;
        this.totalPaid = totalPaid;
        this.overdue = overdue;
        this.recentInvoices = recentInvoices;
    }

    // Getters and Setters
    public BigDecimal getTotalOutstanding() {
        return totalOutstanding;
    }

    public void setTotalOutstanding(BigDecimal totalOutstanding) {
        this.totalOutstanding = totalOutstanding;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public BigDecimal getOverdue() {
        return overdue;
    }

    public void setOverdue(BigDecimal overdue) {
        this.overdue = overdue;
    }

    public List<Invoice> getRecentInvoices() {
        return recentInvoices;
    }

    public void setRecentInvoices(List<Invoice> recentInvoices) {
        this.recentInvoices = recentInvoices;
    }
}