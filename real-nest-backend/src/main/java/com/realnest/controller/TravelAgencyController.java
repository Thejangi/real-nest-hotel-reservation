package com.realnest.controller;

import com.realnest.entity.Booking;
import com.realnest.entity.TravelAgency;
import com.realnest.service.TravelAgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/travel-agencies")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class TravelAgencyController {

    @Autowired
    private TravelAgencyService travelAgencyService;

    // Create a travel agency
    @PostMapping
    public TravelAgency createAgency(@RequestBody TravelAgency agency) {
        return travelAgencyService.createAgency(agency);
    }

    // Get all travel agencies
    @GetMapping
    public List<TravelAgency> getAllAgencies() {
        return travelAgencyService.getAllAgencies();
    }

    // Get a specific agency by ID
    @GetMapping("/{id}")
    public TravelAgency getAgencyById(@PathVariable Long id) {
        return travelAgencyService.getAgencyById(id);
    }

    // Delete a travel agency
    @DeleteMapping("/{id}")
    public void deleteAgency(@PathVariable Long id) {
        travelAgencyService.deleteAgency(id);
    }

    // Bulk booking for a travel agency
    @PostMapping("/{agencyId}/bulk-bookings")
    public List<Booking> createBulkBookings(@PathVariable Long agencyId, @RequestBody List<Booking> bookings) {
        return travelAgencyService.createBulkBooking(agencyId, bookings);
    }
}
