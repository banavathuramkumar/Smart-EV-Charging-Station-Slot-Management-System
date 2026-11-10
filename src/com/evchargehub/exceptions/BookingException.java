package com.evchargehub.exceptions;

/**
 * Thrown when a slot booking operation fails (e.g., date conflict, duplicate booking).
 * Demonstrates multilevel user-defined exception hierarchy.
 */
public class BookingException extends EVChargeHubException {
    private int requestedSlotId;

    public BookingException(String message) {
        super(message);
    }

    public BookingException(String message, int requestedSlotId) {
        super(message + " [Slot ID: " + requestedSlotId + "]");
        this.requestedSlotId = requestedSlotId;
    }

    public int getRequestedSlotId() {
        return requestedSlotId;
    }
}
