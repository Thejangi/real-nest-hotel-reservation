package com.realnest.service.impl;

import com.realnest.entity.Booking;
import com.realnest.entity.TravelAgency;
import com.realnest.repository.BookingRepository;
import com.realnest.repository.TravelAgencyRepository;
import com.realnest.service.TravelAgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TravelAgencyServiceImpl implements TravelAgencyService {

    @Autowired
    private TravelAgencyRepository travelAgencyRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    public TravelAgency createAgency(TravelAgency agency) {
        return travelAgencyRepository.save(agency);
    }

    @Override
    public List<TravelAgency> getAllAgencies() {
        return travelAgencyRepository.findAll();
    }

    @Override
    public TravelAgency getAgencyById(Long id) {
        return travelAgencyRepository.findById(id).orElseThrow();
    }

    @Override
    public void deleteAgency(Long id) {
        travelAgencyRepository.deleteById(id);
    }

    // Bulk booking logic with discount/prepayment enforcement
    @Override
    public List<Booking> createBulkBooking(Long agencyId, List<Booking> bookings) {
        TravelAgency agency = travelAgencyRepository.findById(agencyId).orElseThrow();

        BigDecimal discount = BigDecimal.valueOf(agency.getDiscountPercentage());

        for (Booking booking : bookings) {
            // Set the travel agency ID
            booking.setTravelAgencyId(agencyId);

            // Apply discount to total amount if it exists
            if (booking.getTotalAmount() != null) {
                BigDecimal originalAmount = booking.getTotalAmount();
                BigDecimal discountedAmount = originalAmount
                        .subtract(originalAmount.multiply(discount).divide(BigDecimal.valueOf(100)));
                booking.setTotalAmount(discountedAmount);
            }

            // Enforce prepayment rule
            if (agency.isPrepaymentRequired()) {
                booking.setPaymentStatus("PAID");
            } else {
                booking.setPaymentStatus("PENDING");
            }

            // Mark booking as bulk-agency type
            booking.setBookingStatus("BULK_BOOKING");
        }

        return bookingRepository.saveAll(bookings);
    }
}