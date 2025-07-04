package com.realnest.controller;

import com.realnest.entity.Booking;
import com.realnest.entity.TravelAgency;
import com.realnest.service.TravelAgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<TravelAgency> createAgency(@RequestBody TravelAgency agency) {
        try {
            TravelAgency createdAgency = travelAgencyService.createAgency(agency);
            return new ResponseEntity<>(createdAgency, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all travel agencies
    @GetMapping
    public ResponseEntity<List<TravelAgency>> getAllAgencies() {
        try {
            List<TravelAgency> agencies = travelAgencyService.getAllAgencies();
            return new ResponseEntity<>(agencies, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get a specific agency by ID
    @GetMapping("/{id}")
    public ResponseEntity<TravelAgency> getAgencyById(@PathVariable Long id) {
        try {
            TravelAgency agency = travelAgencyService.getAgencyById(id);
            return new ResponseEntity<>(agency, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Update a travel agency - THIS WAS MISSING!
    @PutMapping("/{id}")
    public ResponseEntity<TravelAgency> updateAgency(@PathVariable Long id, @RequestBody TravelAgency agency) {
        try {
            TravelAgency updatedAgency = travelAgencyService.updateAgency(id, agency);
            return new ResponseEntity<>(updatedAgency, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // Delete a travel agency
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteAgency(@PathVariable Long id) {
        try {
            travelAgencyService.deleteAgency(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Bulk booking for a travel agency
    @PostMapping("/{agencyId}/bulk-bookings")
    public ResponseEntity<List<Booking>> createBulkBookings(@PathVariable Long agencyId,
            @RequestBody List<Booking> bookings) {
        try {
            List<Booking> createdBookings = travelAgencyService.createBulkBooking(agencyId, bookings);
            return new ResponseEntity<>(createdBookings, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}