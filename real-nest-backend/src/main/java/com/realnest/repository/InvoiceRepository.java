package com.realnest.repository;

import com.realnest.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByStatus(String status);

    List<Invoice> findByAgencyId(Long agencyId);

    List<Invoice> findByDueDateBeforeAndStatus(LocalDate date, String status);

    List<Invoice> findByBookingId(Long bookingId);

    Invoice findByInvoiceNumber(String invoiceNumber);
}