package com.realnest.service;

import com.realnest.entity.Booking;
import com.realnest.entity.TravelAgency;

import java.util.List;

public interface TravelAgencyService {

    TravelAgency createAgency(TravelAgency agency);

    List<TravelAgency> getAllAgencies();

    TravelAgency getAgencyById(Long id);

    TravelAgency updateAgency(Long id, TravelAgency agency);

    void deleteAgency(Long id);

    List<Booking> createBulkBooking(Long agencyId, List<Booking> bookings);
}