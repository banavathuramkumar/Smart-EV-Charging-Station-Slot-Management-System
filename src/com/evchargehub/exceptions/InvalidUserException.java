package com.evchargehub.exceptions;

/**
 * Thrown when user credentials or permissions are invalid.
 */
public class InvalidUserException extends EVChargeHubException {
    public InvalidUserException(String message) {
        super(message);
    }

    public InvalidUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
