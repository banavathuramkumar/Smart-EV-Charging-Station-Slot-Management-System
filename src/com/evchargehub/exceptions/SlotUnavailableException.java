package com.evchargehub.exceptions;

/**
 * Thrown specifically when a requested charging slot is occupied, reserved, or in maintenance.
 * Demonstrates multilevel exception inheritance (SlotUnavailableException -> BookingException -> EVChargeHubException -> Exception).
 */
public class SlotUnavailableException extends BookingException {
    private String slotStatus;

    public SlotUnavailableException(int slotId, String slotStatus) {
        super("Charging slot is not available for booking. Current status: " + slotStatus, slotId);
        this.slotStatus = slotStatus;
    }

    public String getSlotStatus() {
        return slotStatus;
    }
}
