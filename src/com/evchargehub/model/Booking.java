package com.evchargehub.model;

/**
 * Represents a slot reservation booking made by an EV User.
 */
public class Booking {
    private int bookingId;
    private int userId;
    private int stationId;
    private int slotId;
    private int vehicleId;
    private String bookingDate; // YYYY-MM-DD
    private String startTime;   // HH:mm
    private double durationHours;
    private String endTime;     // HH:mm
    private BookingStatus status;
    private String createdAt;

    // Additional display fields joined from queries
    private String stationName;
    private String slotNumber;
    private String vehicleInfo;
    private String userName;

    public Booking() {
        this.status = BookingStatus.CONFIRMED;
    }

    public Booking(int userId, int stationId, int slotId, int vehicleId, String bookingDate, String startTime, double durationHours, String endTime) {
        this(0, userId, stationId, slotId, vehicleId, bookingDate, startTime, durationHours, endTime, BookingStatus.CONFIRMED, "");
    }

    public Booking(int bookingId, int userId, int stationId, int slotId, int vehicleId, String bookingDate, String startTime, double durationHours, String endTime, BookingStatus status, String createdAt) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.stationId = stationId;
        this.slotId = slotId;
        this.vehicleId = vehicleId;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.durationHours = durationHours;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public int getSlotId() {
        return slotId;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public double getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(double durationHours) {
        this.durationHours = durationHours;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getSlotNumber() {
        return slotNumber;
    }

    public void setSlotNumber(String slotNumber) {
        this.slotNumber = slotNumber;
    }

    public String getVehicleInfo() {
        return vehicleInfo;
    }

    public void setVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "Booking #" + bookingId + " [" + bookingDate + " " + startTime + "-" + endTime + "] - " + status;
    }
}
