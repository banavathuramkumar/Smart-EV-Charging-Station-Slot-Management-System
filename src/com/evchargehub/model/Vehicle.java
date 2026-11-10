package com.evchargehub.model;

/**
 * Represents an Electric Vehicle registered by an EV user.
 * Demonstrates:
 * - Encapsulation
 * - Method Overloading (Compile-time Polymorphism)
 * - Constructor Overloading
 */
public class Vehicle {
    private int vehicleId;
    private int ownerId;
    private String registrationNumber;
    private String brand;
    private String model;
    private double batteryCapacityKWh;
    private String connectorType; // e.g. "CCS2", "Type 2", "CHAdeMO", "GB/T"

    public Vehicle() {}

    // Overloaded constructor 1
    public Vehicle(int ownerId, String registrationNumber, String brand, String model, double batteryCapacityKWh, String connectorType) {
        this(0, ownerId, registrationNumber, brand, model, batteryCapacityKWh, connectorType);
    }

    // Overloaded constructor 2
    public Vehicle(int vehicleId, int ownerId, String registrationNumber, String brand, String model, double batteryCapacityKWh, String connectorType) {
        this.vehicleId = vehicleId;
        this.ownerId = ownerId;
        this.registrationNumber = registrationNumber;
        this.brand = brand;
        this.model = model;
        this.batteryCapacityKWh = batteryCapacityKWh;
        this.connectorType = connectorType;
    }

    // Method Overloading 1: Estimate charging time from 0% to 100%
    public double estimateChargingTimeHours(double chargerPowerKW) {
        if (chargerPowerKW <= 0) return 0.0;
        return this.batteryCapacityKWh / chargerPowerKW;
    }

    // Method Overloading 2: Estimate charging time from current percentage to target percentage
    public double estimateChargingTimeHours(double chargerPowerKW, double currentPercentage, double targetPercentage) {
        if (chargerPowerKW <= 0 || targetPercentage <= currentPercentage) return 0.0;
        double energyNeeded = this.batteryCapacityKWh * ((targetPercentage - currentPercentage) / 100.0);
        return energyNeeded / chargerPowerKW;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getBatteryCapacityKWh() {
        return batteryCapacityKWh;
    }

    public void setBatteryCapacityKWh(double batteryCapacityKWh) {
        this.batteryCapacityKWh = batteryCapacityKWh;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    @Override
    public String toString() {
        return brand + " " + model + " (" + registrationNumber + ") - " + connectorType + " [" + batteryCapacityKWh + " kWh]";
    }
}
