package com.evchargehub.util;

import com.evchargehub.exceptions.InvalidUserException;
import java.util.regex.Pattern;

/**
 * Validation utility for input sanitation and validation across the application.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern VEHICLE_REG_PATTERN = Pattern.compile("^[A-Z0-9 -]{5,15}$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim().replaceAll("[\\s-]", "")).matches();
    }

    public static boolean isValidVehicleReg(String reg) {
        return reg != null && VEHICLE_REG_PATTERN.matcher(reg.trim()).matches();
    }

    public static void validateUserRegistration(String username, String password, String fullName, String email, String phone) throws InvalidUserException {
        if (username == null || username.trim().length() < 3) {
            throw new InvalidUserException("Username must be at least 3 characters long.");
        }
        if (password == null || password.length() < 6) {
            throw new InvalidUserException("Password must be at least 6 characters long.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new InvalidUserException("Full name cannot be empty.");
        }
        if (email != null && !email.trim().isEmpty() && !isValidEmail(email)) {
            throw new InvalidUserException("Invalid email format (e.g. user@example.com).");
        }
        if (phone != null && !phone.trim().isEmpty() && !isValidPhone(phone)) {
            throw new InvalidUserException("Phone number must contain 10 digits.");
        }
    }
}
