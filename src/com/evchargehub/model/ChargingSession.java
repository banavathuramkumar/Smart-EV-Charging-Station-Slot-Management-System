package com.evchargehub.model;

/**
 * Represents an active or completed EV Charging Session.
 */
public class ChargingSession {
    private int sessionId;
    private int bookingId;
    private int userId;
    private int slotId;
    private String startTime;
    private String endTime;
    private double energyDeliveredKWh;
    private double totalCost;
    private String status; // "IN_PROGRESS", "COMPLETED", "STOPPED"

    public ChargingSession() {
        this.status = "IN_PROGRESS";
    }

    public ChargingSession(int bookingId, int userId, int slotId, String startTime) {
        this(0, bookingId, userId, slotId, startTime, null, 0.0, 0.0, "IN_PROGRESS");
    }

    public ChargingSession(int sessionId, int bookingId, int userId, int slotId, String startTime, String endTime, double energyDeliveredKWh, double totalCost, String status) {
        this.sessionId = sessionId;
        this.bookingId = bookingId;
        this.userId = userId;
        this.slotId = slotId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.energyDeliveredKWh = energyDeliveredKWh;
        this.totalCost = totalCost;
        this.status = status;
    }

    // Static helper formula for simulated charging calculations
    public static double calculateEnergyDelivered(double chargingPowerKW, double durationHours) {
        return chargingPowerKW * durationHours;
    }

    public static double calculateCost(double energyKWh, double pricePerKWh) {
        return energyKWh * pricePerKWh;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getEnergyDeliveredKWh() {
        return energyDeliveredKWh;
    }

    public void setEnergyDeliveredKWh(double energyDeliveredKWh) {
        this.energyDeliveredKWh = energyDeliveredKWh;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Session #" + sessionId + " (Booking #" + bookingId + ") - " + String.format("%.2f", energyDeliveredKWh) + " kWh [$" + String.format("%.2f", totalCost) + "]";
    }
}
