package com.evchargehub.service;

import com.evchargehub.dao.BookingDAO;
import com.evchargehub.dao.SessionDAO;
import com.evchargehub.dao.SlotDAO;
import com.evchargehub.exceptions.BookingException;
import com.evchargehub.model.*;
import com.evchargehub.threads.ChargingSessionThread;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing simulated multithreaded EV charging sessions.
 */
public class ChargingService {

    private final SessionDAO sessionDAO;
    private final BookingDAO bookingDAO;
    private final SlotDAO slotDAO;

    // Active in-memory session threads indexed by sessionId
    private static final ConcurrentHashMap<Integer, ChargingSessionThread> activeThreads = new ConcurrentHashMap<>();

    public ChargingService() {
        this.sessionDAO = new SessionDAO();
        this.bookingDAO = new BookingDAO();
        this.slotDAO = new SlotDAO();
    }

    /**
     * Initiates a multithreaded simulated charging session from a confirmed booking.
     */
    public ChargingSession startSession(Booking booking, Vehicle vehicle, ChargingStation station, ChargingSlot slot, ChargingSessionThread.SessionUpdateListener listener) throws SQLException, BookingException {
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingException("Charging can only start for CONFIRMED bookings. Current booking status: " + booking.getStatus());
        }

        String startTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        ChargingSession session = new ChargingSession(booking.getBookingId(), booking.getUserId(), booking.getSlotId(), startTimeStr);

        // Save session record to SQLite
        boolean inserted = sessionDAO.insert(session);
        if (!inserted) {
            throw new SQLException("Failed to create charging session in database.");
        }

        // Mark booking IN_PROGRESS and slot OCCUPIED
        bookingDAO.updateStatus(booking.getBookingId(), BookingStatus.IN_PROGRESS);
        slotDAO.updateStatus(slot.getSlotId(), SlotStatus.OCCUPIED);

        // Launch background ChargingSessionThread (Multithreading)
        double powerKW = slot.getChargingPowerKW() > 0 ? slot.getChargingPowerKW() : station.getChargingSpeedKW();
        double price = station.getPricePerKWh();
        double capacity = vehicle != null ? vehicle.getBatteryCapacityKWh() : 50.0;
        double currentBattery = 20.0; // Simulated start battery percentage

        ChargingSessionThread sessionThread = new ChargingSessionThread(
            session, powerKW, price, capacity, currentBattery,
            new ChargingSessionThread.SessionUpdateListener() {
                @Override
                public void onProgress(double energyKWh, double cost, int elapsedSecs, double batteryPct) {
                    // Update database periodically
                    try {
                        sessionDAO.update(session);
                    } catch (SQLException ignored) {}

                    if (listener != null) {
                        listener.onProgress(energyKWh, cost, elapsedSecs, batteryPct);
                    }
                }

                @Override
                public void onComplete(ChargingSession completedSession) {
                    try {
                        stopSession(completedSession, station.getStationId());
                    } catch (SQLException e) {
                        System.err.println("Error completing session in DB: " + e.getMessage());
                    }
                    if (listener != null) {
                        listener.onComplete(completedSession);
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    if (listener != null) {
                        listener.onError(errorMessage);
                    }
                }
            }
        );

        activeThreads.put(session.getSessionId(), sessionThread);
        sessionThread.start(); // Start background simulation thread

        return session;
    }

    /**
     * Stops an active charging session and finalizes database records.
     */
    public boolean stopSession(ChargingSession session, int stationId) throws SQLException {
        ChargingSessionThread thread = activeThreads.remove(session.getSessionId());
        if (thread != null && thread.isChargingActive()) {
            thread.stopCharging();
        }

        if ("COMPLETED".equalsIgnoreCase(session.getStatus()) && session.getEndTime() != null) {
            return true; // Already finalized
        }

        session.setStatus("COMPLETED");
        if (session.getEndTime() == null) {
            session.setEndTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        }

        // If stationId <= 0, query it from the slot directly
        if (stationId <= 0) {
            ChargingSlot slot = slotDAO.findById(session.getSlotId());
            if (slot != null) {
                stationId = slot.getStationId();
            }
        }

        return sessionDAO.completeSessionAtomic(session, stationId);
    }

    public ChargingSession getSessionById(int sessionId) throws SQLException {
        return sessionDAO.findById(sessionId);
    }

    public ChargingSessionThread getActiveThread(int sessionId) {
        return activeThreads.get(sessionId);
    }
}
