package com.evchargehub.service;

import com.evchargehub.dao.UserDAO;
import com.evchargehub.exceptions.InvalidUserException;
import com.evchargehub.model.Admin;
import com.evchargehub.model.Person;
import com.evchargehub.model.User;
import com.evchargehub.util.PasswordUtil;
import com.evchargehub.util.ValidationUtil;

import java.sql.SQLException;

/**
 * Service handling authentication, login, registration, and active session state.
 */
public class AuthenticationService {

    private final UserDAO userDAO;
    private static Person currentUser = null; // Currently logged-in user or admin

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticates a user or admin against the database.
     * Demonstrates custom exception throwing (InvalidUserException).
     */
    public Person login(String username, String plainPassword) throws InvalidUserException, SQLException {
        if (username == null || username.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
            throw new InvalidUserException("Username and password cannot be empty.");
        }

        Person person = userDAO.findByUsername(username.trim());
        if (person == null) {
            throw new InvalidUserException("Account with username '" + username + "' does not exist.");
        }

        if (!PasswordUtil.verifyPassword(plainPassword, person.getPasswordHash())) {
            throw new InvalidUserException("Invalid password. Please verify and try again.");
        }

        currentUser = person;
        System.out.println("[AuthenticationService] Successfully authenticated: " + person.getSystemBadge());
        return person;
    }

    /**
     * Registers a new EV Driver (User).
     */
    public User registerUser(String username, String plainPassword, String fullName, String email, String phone, String licenseNumber) throws InvalidUserException, SQLException {
        ValidationUtil.validateUserRegistration(username, plainPassword, fullName, email, phone);

        // Check if username already exists
        if (userDAO.findByUsername(username.trim()) != null) {
            throw new InvalidUserException("Username '" + username + "' is already taken. Please choose another.");
        }

        String passwordHash = PasswordUtil.hashPassword(plainPassword);
        User newUser = new User(0, username.trim(), passwordHash, fullName.trim(), email.trim(), phone.trim(), 100.0);
        newUser.setDrivingLicenseNumber(licenseNumber != null ? licenseNumber.trim() : "");

        boolean inserted = userDAO.insert(newUser);
        if (!inserted) {
            throw new InvalidUserException("Registration failed due to an unexpected database error.");
        }

        System.out.println("[AuthenticationService] Registered new user: " + newUser.getUsername() + " (ID: " + newUser.getId() + ")");
        return newUser;
    }

    public static Person getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        if (currentUser != null) {
            System.out.println("[AuthenticationService] Logged out user: " + currentUser.getUsername());
        }
        currentUser = null;
    }

    public static boolean isAdminLoggedIn() {
        return currentUser instanceof Admin;
    }
}
