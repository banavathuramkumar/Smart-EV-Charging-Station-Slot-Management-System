package com.evchargehub.dao;

import com.evchargehub.database.DatabaseConnection;
import com.evchargehub.exceptions.BookingException;
import com.evchargehub.exceptions.SlotUnavailableException;
import com.evchargehub.model.Booking;
import com.evchargehub.model.BookingStatus;
import com.evchargehub.model.SlotStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Bookings with concurrency protection and atomic transactions.
 * Demonstrates:
 * - Synchronization (monitor lock to prevent race conditions during booking)
 * - SQLite Transaction handling (setAutoCommit(false), commit, rollback)
 * - Custom exception handling (SlotUnavailableException, BookingException)
 */
public class BookingDAO implements GenericDAO<Booking> {

    // Global monitor lock for atomic slot reservations across threads
    private static final Object BOOKING_LOCK = new Object();

    @Override
    public boolean insert(Booking booking) throws SQLException {
        try {
            return createBookingAtomic(booking);
        } catch (BookingException e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    /**
     * Atomically creates a booking while checking for slot availability and conflicting time intervals.
     * Uses Java synchronization + JDBC manual transaction (setAutoCommit(false)).
     */
    public boolean createBookingAtomic(Booking b) throws SQLException, BookingException {
        synchronized (BOOKING_LOCK) {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false); // Begin Transaction

                // 1. Verify slot current status
                String slotCheckSql = "SELECT status FROM charging_slots WHERE slot_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(slotCheckSql)) {
                    ps.setInt(1, b.getSlotId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new BookingException("Charging slot not found.", b.getSlotId());
                        }
                        String currentStatus = rs.getString("status");
                        if ("MAINTENANCE".equalsIgnoreCase(currentStatus)) {
                            throw new SlotUnavailableException(b.getSlotId(), "MAINTENANCE");
                        }
                    }
                }

                // 2. Check for conflicting active bookings for the same slot and date
                // Overlap occurs if (NewStart < ExistingEnd) AND (NewEnd > ExistingStart)
                String conflictCheckSql = """
                    SELECT booking_id, start_time, end_time FROM bookings
                    WHERE slot_id = ? AND booking_date = ? AND status IN ('CONFIRMED', 'IN_PROGRESS')
                    AND NOT (end_time <= ? OR start_time >= ?)
                """;
                try (PreparedStatement ps = conn.prepareStatement(conflictCheckSql)) {
                    ps.setInt(1, b.getSlotId());
                    ps.setString(2, b.getBookingDate());
                    ps.setString(3, b.getStartTime());
                    ps.setString(4, b.getEndTime());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int conflictId = rs.getInt("booking_id");
                            String cStart = rs.getString("start_time");
                            String cEnd = rs.getString("end_time");
                            throw new BookingException("Slot " + b.getSlotId() + " is already booked between " + cStart + " and " + cEnd + " on " + b.getBookingDate() + " (Conflict with Booking #" + conflictId + ").");
                        }
                    }
                }

                // 3. Insert the new booking
                String insertSql = """
                    INSERT INTO bookings (user_id, station_id, slot_id, vehicle_id, booking_date, start_time, duration_hours, end_time, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, b.getUserId());
                    ps.setInt(2, b.getStationId());
                    ps.setInt(3, b.getSlotId());
                    ps.setInt(4, b.getVehicleId());
                    ps.setString(5, b.getBookingDate());
                    ps.setString(6, b.getStartTime());
                    ps.setDouble(7, b.getDurationHours());
                    ps.setString(8, b.getEndTime());
                    ps.setString(9, BookingStatus.CONFIRMED.name());

                    int affected = ps.executeUpdate();
                    if (affected > 0) {
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                b.setBookingId(rs.getInt(1));
                            }
                        }
                    } else {
                        conn.rollback();
                        return false;
                    }
                }

                // 4. Update the slot status to RESERVED
                String updateSlotSql = "UPDATE charging_slots SET status = 'RESERVED' WHERE slot_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSlotSql)) {
                    ps.setInt(1, b.getSlotId());
                    ps.executeUpdate();
                }

                // 5. Update available slot count in station
                String updateStationSql = """
                    UPDATE charging_stations 
                    SET available_slots = (SELECT COUNT(*) FROM charging_slots WHERE station_id = ? AND status = 'AVAILABLE')
                    WHERE station_id = ?
                """;
                try (PreparedStatement ps = conn.prepareStatement(updateStationSql)) {
                    ps.setInt(1, b.getStationId());
                    ps.setInt(2, b.getStationId());
                    ps.executeUpdate();
                }

                conn.commit(); // Transaction Succeeded!
                System.out.println("[BookingDAO] Atomic booking created successfully: Booking #" + b.getBookingId());
                return true;

            } catch (SQLException | BookingException e) {
                if (conn != null) {
                    try {
                        conn.rollback(); // Rollback on any failure
                        System.out.println("[BookingDAO] Transaction rolled back due to: " + e.getMessage());
                    } catch (SQLException rollbackEx) {
                        System.err.println("Rollback failed: " + rollbackEx.getMessage());
                    }
                }
                throw e;
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException closeEx) {
                        System.err.println("Failed to close connection: " + closeEx.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Cancels an existing booking, freeing up the slot if no other active booking holds it.
     */
    public boolean cancelBooking(int bookingId) throws SQLException {
        synchronized (BOOKING_LOCK) {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false);

                // Fetch booking details
                String fetchSql = "SELECT slot_id, station_id, status FROM bookings WHERE booking_id = ?";
                int slotId = 0;
                int stationId = 0;
                String status = "";
                try (PreparedStatement ps = conn.prepareStatement(fetchSql)) {
                    ps.setInt(1, bookingId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            slotId = rs.getInt("slot_id");
                            stationId = rs.getInt("station_id");
                            status = rs.getString("status");
                        }
                    }
                }

                if (slotId == 0 || "CANCELLED".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)) {
                    conn.rollback();
                    return false;
                }

                // Update booking to CANCELLED
                String cancelSql = "UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(cancelSql)) {
                    ps.setInt(1, bookingId);
                    ps.executeUpdate();
                }

                // Check if any other active booking for this slot
                String checkActiveSql = "SELECT COUNT(*) FROM bookings WHERE slot_id = ? AND status IN ('CONFIRMED', 'IN_PROGRESS')";
                boolean hasOtherActive = false;
                try (PreparedStatement ps = conn.prepareStatement(checkActiveSql)) {
                    ps.setInt(1, slotId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            hasOtherActive = true;
                        }
                    }
                }

                if (!hasOtherActive) {
                    String freeSlotSql = "UPDATE charging_slots SET status = 'AVAILABLE' WHERE slot_id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(freeSlotSql)) {
                        ps.setInt(1, slotId);
                        ps.executeUpdate();
                    }

                    String updateStnSql = """
                        UPDATE charging_stations 
                        SET available_slots = (SELECT COUNT(*) FROM charging_slots WHERE station_id = ? AND status = 'AVAILABLE')
                        WHERE station_id = ?
                    """;
                    try (PreparedStatement ps = conn.prepareStatement(updateStnSql)) {
                        ps.setInt(1, stationId);
                        ps.setInt(2, stationId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                System.out.println("[BookingDAO] Booking #" + bookingId + " cancelled successfully.");
                return true;

            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException ignored) {}
                }
                throw e;
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException ignored) {}
                }
            }
        }
    }

    public boolean updateStatus(int bookingId, BookingStatus status) throws SQLException {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Booking entity) throws SQLException {
        return updateStatus(entity.getBookingId(), entity.getStatus());
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return cancelBooking(id);
    }

    @Override
    public Booking findById(int id) throws SQLException {
        String sql = getBookingJoinQuery() + " WHERE b.booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBooking(rs);
                }
            }
        }
        return null;
    }

    public List<Booking> findByUserId(int userId) throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = getBookingJoinQuery() + " WHERE b.user_id = ? ORDER BY b.booking_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBooking(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Booking> findAll() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = getBookingJoinQuery() + " ORDER BY b.booking_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToBooking(rs));
            }
        }
        return list;
    }

    public int countTotalBookings() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private String getBookingJoinQuery() {
        return """
            SELECT b.*, u.full_name as user_name, s.name as station_name, sl.slot_number,
                   v.brand || ' ' || v.model || ' (' || v.reg_number || ')' as vehicle_info
            FROM bookings b
            LEFT JOIN users u ON b.user_id = u.id
            LEFT JOIN charging_stations s ON b.station_id = s.station_id
            LEFT JOIN charging_slots sl ON b.slot_id = sl.slot_id
            LEFT JOIN vehicles v ON b.vehicle_id = v.vehicle_id
        """;
    }

    private Booking mapRowToBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking(
            rs.getInt("booking_id"),
            rs.getInt("user_id"),
            rs.getInt("station_id"),
            rs.getInt("slot_id"),
            rs.getInt("vehicle_id"),
            rs.getString("booking_date"),
            rs.getString("start_time"),
            rs.getDouble("duration_hours"),
            rs.getString("end_time"),
            BookingStatus.valueOf(rs.getString("status")),
            rs.getString("created_at")
        );
        b.setUserName(rs.getString("user_name"));
        b.setStationName(rs.getString("station_name"));
        b.setSlotNumber(rs.getString("slot_number"));
        b.setVehicleInfo(rs.getString("vehicle_info"));
        return b;
    }
}
