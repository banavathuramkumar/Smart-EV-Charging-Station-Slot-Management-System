package com.evchargehub.exceptions;

/**
 * Base user-defined exception for the EV ChargeHub system.
 * - User-defined exceptions
 * - Subclassing java.lang.Exception
 * - Exception hierarchy
 * - Constructors and message passing to super(message)
 */
public class EVChargeHubException extends Exception {
    private final long timestamp;

    public EVChargeHubException(String message) {
        super(message);
        this.timestamp = System.currentTimeMillis();
    }

    public EVChargeHubException(String message, Throwable cause) {
        super(message, cause);
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
