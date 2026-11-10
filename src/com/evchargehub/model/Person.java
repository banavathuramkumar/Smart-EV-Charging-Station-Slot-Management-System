package com.evchargehub.model;

/**
 * Abstract base class representing a Person in the EV ChargeHub system.
 * - Abstract classes
 * - Constructors and constructor overloading
 * - this reference and super usage in subclasses
 * - static and final keywords
 * - Encapsulation and Access specifiers
 */
public abstract class Person {
    public static final String SYSTEM_ORGANIZATION = "EV ChargeHub Network Inc.";

    protected int id;
    protected String username;
    protected String passwordHash;
    protected String fullName;
    protected String email;
    protected String phone;
    protected String role; // "USER" or "ADMIN"

    // Default constructor
    public Person() {
        this(0, "", "", "", "", "", "USER");
    }

    // Overloaded constructor with basic credentials
    public Person(String username, String passwordHash, String fullName, String role) {
        this(0, username, passwordHash, fullName, "", "", role);
    }

    // Full constructor (demonstrating constructor overloading and 'this' reference)
    public Person(int id, String username, String passwordHash, String fullName, String email, String phone, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    // Abstract method to be overridden by subclasses (Runtime Polymorphism / Dynamic Method Dispatch)
    public abstract String getRoleDescription();

    // Final method that cannot be overridden by subclasses (final with inheritance)
    public final String getSystemBadge() {
        return "[" + this.role + "] " + this.fullName + " (" + this.username + ")";
    }

    // Getters and Setters demonstrating encapsulation
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return getSystemBadge();
    }
}
