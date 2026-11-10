package com.evchargehub.model;

/**
 * Admin entity representing a Station Network Administrator.
 * Demonstrates:
 * - Inheritance (extends Person)
 * - super(...) constructor calls
 * - Method overriding
 */
public class Admin extends Person {
    private String department;
    private int accessLevel; // 1 to 5

    public Admin() {
        super();
        this.role = "ADMIN";
        this.department = "Operations";
        this.accessLevel = 5;
    }

    public Admin(String username, String passwordHash, String fullName, String email, String phone) {
        super(0, username, passwordHash, fullName, email, phone, "ADMIN");
        this.department = "Station Operations";
        this.accessLevel = 5;
    }

    public Admin(int id, String username, String passwordHash, String fullName, String email, String phone, String department, int accessLevel) {
        super(id, username, passwordHash, fullName, email, phone, "ADMIN");
        this.department = department;
        this.accessLevel = accessLevel;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Override
    public String getRoleDescription() {
        return "Network Administrator: Full control over stations, slots, users, analytics, and reports.";
    }
}
