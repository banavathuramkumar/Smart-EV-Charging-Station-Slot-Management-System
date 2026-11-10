package com.evchargehub.model;

/**
 * Represents a physical EV Charging Station location.
 * Demonstrates:
 * - Encapsulation
 * - Comparable interface implementation for natural ordering (used in TreeSet)
 */
public class ChargingStation implements Comparable<ChargingStation> {
    private int stationId;
    private String name;
    private String location;
    private int totalSlots;
    private int availableSlots;
    private double chargingSpeedKW;
    private double pricePerKWh;
    private String status; // "ACTIVE", "INACTIVE", "MAINTENANCE"

    public ChargingStation() {
        this.status = "ACTIVE";
    }

    public ChargingStation(String name, String location, int totalSlots, double chargingSpeedKW, double pricePerKWh, String status) {
        this(0, name, location, totalSlots, totalSlots, chargingSpeedKW, pricePerKWh, status);
    }

    public ChargingStation(int stationId, String name, String location, int totalSlots, int availableSlots, double chargingSpeedKW, double pricePerKWh, String status) {
        this.stationId = stationId;
        this.name = name;
        this.location = location;
        this.totalSlots = totalSlots;
        this.availableSlots = availableSlots;
        this.chargingSpeedKW = chargingSpeedKW;
        this.pricePerKWh = pricePerKWh;
        this.status = status;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    public double getChargingSpeedKW() {
        return chargingSpeedKW;
    }

    public void setChargingSpeedKW(double chargingSpeedKW) {
        this.chargingSpeedKW = chargingSpeedKW;
    }

    public double getPricePerKWh() {
        return pricePerKWh;
    }

    public void setPricePerKWh(double pricePerKWh) {
        this.pricePerKWh = pricePerKWh;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    @Override
    public int compareTo(ChargingStation other) {
        if (other == null) return 1;
        int locCompare = this.location.compareToIgnoreCase(other.location);
        if (locCompare != 0) return locCompare;
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        return name + " (" + location + ") - " + chargingSpeedKW + " kW [$" + String.format("%.2f", pricePerKWh) + "/kWh]";
    }
}
