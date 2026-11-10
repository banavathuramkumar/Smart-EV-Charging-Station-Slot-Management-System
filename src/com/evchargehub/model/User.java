package com.evchargehub.model;

/**
 * User entity representing an EV driver.
 * Demonstrates:
 * - Inheritance (extends Person)
 * - Constructor calling super(...)
 * - Method overriding (getRoleDescription)
 */
public class User extends Person {
    private String drivingLicenseNumber;
    private double walletBalance;

    public User() {
        super();
        this.role = "USER";
        this.walletBalance = 0.0;
    }

    public User(String username, String passwordHash, String fullName, String email, String phone) {
        super(0, username, passwordHash, fullName, email, phone, "USER");
        this.walletBalance = 100.0; // Default simulated introductory wallet credit
    }

    public User(int id, String username, String passwordHash, String fullName, String email, String phone, double walletBalance) {
        super(id, username, passwordHash, fullName, email, phone, "USER");
        this.walletBalance = walletBalance;
    }

    public String getDrivingLicenseNumber() {
        return drivingLicenseNumber;
    }

    public void setDrivingLicenseNumber(String drivingLicenseNumber) {
        this.drivingLicenseNumber = drivingLicenseNumber;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    @Override
    public String getRoleDescription() {
        return "Electric Vehicle Driver / Consumer: Can reserve slots, simulate charging, and view billing.";
    }
}
