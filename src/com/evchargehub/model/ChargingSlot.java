package com.evchargehub.model;

/**
 * Represents an individual charging connector bay / slot at a station.
 */
public class ChargingSlot {
    private int slotId;
    private int stationId;
    private String slotNumber;
    private String connectorType;
    private double chargingPowerKW;
    private SlotStatus status;

    public ChargingSlot() {
        this.status = SlotStatus.AVAILABLE;
    }

    public ChargingSlot(int stationId, String slotNumber, String connectorType, double chargingPowerKW, SlotStatus status) {
        this(0, stationId, slotNumber, connectorType, chargingPowerKW, status);
    }

    public ChargingSlot(int slotId, int stationId, String slotNumber, String connectorType, double chargingPowerKW, SlotStatus status) {
        this.slotId = slotId;
        this.stationId = stationId;
        this.slotNumber = slotNumber;
        this.connectorType = connectorType;
        this.chargingPowerKW = chargingPowerKW;
        this.status = status;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public double getChargingPowerKW() {
        return chargingPowerKW;
    }

    public void setChargingPowerKW(double chargingPowerKW) {
        this.chargingPowerKW = chargingPowerKW;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return this.status == SlotStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return slotNumber + " [" + connectorType + ", " + chargingPowerKW + " kW] - " + status;
    }
}
