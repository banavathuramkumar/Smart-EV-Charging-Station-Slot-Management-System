package com.evchargehub.exceptions;

/**
 * Thrown when an underlying JDBC database operation encounters an error.
 */
public class DatabaseOperationException extends EVChargeHubException {
    public DatabaseOperationException(String message) {
        super(message);
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
