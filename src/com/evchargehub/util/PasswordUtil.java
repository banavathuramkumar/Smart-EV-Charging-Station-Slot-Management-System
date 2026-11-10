package com.evchargehub.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for secure password hashing and verification.
 * Strictly uses Java's built-in standard security libraries.
 */
public class PasswordUtil {

    private static final String SALT = "EVChargeHub_Enterprise_Secure_Salt_2026";

    /**
     * Hashes a plaintext password using SHA-256 and a static salt.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String salted = SALT + plainPassword;
            byte[] encodedhash = digest.digest(salted.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in current JRE", e);
        }
    }

    /**
     * Verifies if a plaintext password matches a stored hash.
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        String calculated = hashPassword(plainPassword);
        return calculated.equalsIgnoreCase(storedHash);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
