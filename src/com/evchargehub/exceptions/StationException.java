package com.evchargehub.exceptions;

/**
 * Thrown when station or slot management validation fails.
 */
public class StationException extends EVChargeHubException {
    public StationException(String message) {
        super(message);
    }
}
