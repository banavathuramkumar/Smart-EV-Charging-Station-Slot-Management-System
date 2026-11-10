package com.evchargehub.service;

import com.evchargehub.dao.BookingDAO;
import com.evchargehub.dao.SlotDAO;
import com.evchargehub.exceptions.BookingException;
import com.evchargehub.exceptions.SlotUnavailableException;
import com.evchargehub.model.Booking;
import com.evchargehub.model.ChargingSlot;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Service managing smart slot reservations, conflict checks, and cancellations.
 */
public class BookingService {

    private final BookingDAO bookingDAO;
    private final SlotDAO slotDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.slotDAO = new SlotDAO();
    }

    /**
     * Creates a new booking with rigorous validation.
     */
    public Booking bookSlot(int userId, int stationId, int slotId, int vehicleId, String dateStr, String startTimeStr, double durationHours) throws BookingException, SQLException {
        // 1. Validate inputs
        if (userId <= 0) throw new BookingException("Invalid user ID.");
        if (stationId <= 0) throw new BookingException("Please select a charging station.");
        if (slotId <= 0) throw new BookingException("Please select a charging slot.");
        if (vehicleId <= 0) throw new BookingException("Please select a registered vehicle.");

        // 2. Validate date (cannot be before today)
        LocalDate bookingDate;
        try {
            bookingDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            if (bookingDate.isBefore(LocalDate.now())) {
                throw new BookingException("Booking date cannot be in the past.");
            }
        } catch (DateTimeParseException e) {
            throw new BookingException("Invalid date format. Expected YYYY-MM-DD.");
        }

        // 3. Validate start time and duration
        LocalTime startTime;
        try {
            startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            throw new BookingException("Invalid start time format. Expected HH:mm (e.g. 14:30).");
        }

        if (bookingDate.isEqual(LocalDate.now()) && startTime.isBefore(LocalTime.now())) {
            throw new BookingException("Booking time cannot be in the past for today's date.");
        }

        if (durationHours < 0.25 || durationHours > 12.0) {
            throw new BookingException("Charging duration must be between 15 minutes (0.25 hrs) and 12 hours.");
        }

        // Calculate end time
        int totalMinutes = (int) Math.round(durationHours * 60);
        LocalTime endTime = startTime.plusMinutes(totalMinutes);
        String endTimeStr = endTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        // 4. Check slot exists and is not under maintenance
        ChargingSlot slot = slotDAO.findById(slotId);
        if (slot == null) {
            throw new BookingException("Charging slot not found.", slotId);
        }
        if (slot.getStatus() == com.evchargehub.model.SlotStatus.MAINTENANCE) {
            throw new SlotUnavailableException(slotId, "MAINTENANCE");
        }

        Booking booking = new Booking(userId, stationId, slotId, vehicleId, dateStr, startTimeStr, durationHours, endTimeStr);

        // Atomic reservation with concurrency control
        boolean success = bookingDAO.createBookingAtomic(booking);
        if (!success) {
            throw new BookingException("Unable to secure reservation due to conflicting request. Please choose another slot or time.");
        }

        return booking;
    }

    public boolean cancelBooking(int bookingId) throws SQLException {
        return bookingDAO.cancelBooking(bookingId);
    }

    public List<Booking> getUserBookings(int userId) throws SQLException {
        return bookingDAO.findByUserId(userId);
    }

    public List<Booking> getAllBookings() throws SQLException {
        return bookingDAO.findAll();
    }

    public Booking getBookingById(int bookingId) throws SQLException {
        return bookingDAO.findById(bookingId);
    }
}
