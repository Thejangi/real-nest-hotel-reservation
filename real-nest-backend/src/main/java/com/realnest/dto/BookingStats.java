package com.realnest.dto;

import java.math.BigDecimal;

public class BookingStats {
    private int activeBlocks;
    private int totalRooms;
    private BigDecimal totalRevenue;
    private double averageDiscount;
    private int confirmedBookings;
    private int pendingBookings;

    // Default constructor
    public BookingStats() {
    }

    // Constructor with all fields
    public BookingStats(int activeBlocks, int totalRooms, BigDecimal totalRevenue,
            double averageDiscount, int confirmedBookings, int pendingBookings) {
        this.activeBlocks = activeBlocks;
        this.totalRooms = totalRooms;
        this.totalRevenue = totalRevenue;
        this.averageDiscount = averageDiscount;
        this.confirmedBookings = confirmedBookings;
        this.pendingBookings = pendingBookings;
    }

    // Getters and Setters
    public int getActiveBlocks() {
        return activeBlocks;
    }

    public void setActiveBlocks(int activeBlocks) {
        this.activeBlocks = activeBlocks;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getAverageDiscount() {
        return averageDiscount;
    }

    public void setAverageDiscount(double averageDiscount) {
        this.averageDiscount = averageDiscount;
    }

    public int getConfirmedBookings() {
        return confirmedBookings;
    }

    public void setConfirmedBookings(int confirmedBookings) {
        this.confirmedBookings = confirmedBookings;
    }

    public int getPendingBookings() {
        return pendingBookings;
    }

    public void setPendingBookings(int pendingBookings) {
        this.pendingBookings = pendingBookings;
    }
}