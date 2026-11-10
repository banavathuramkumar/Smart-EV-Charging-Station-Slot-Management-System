package com.evchargehub.dao;

import com.evchargehub.database.DatabaseConnection;
import com.evchargehub.model.ChargingSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Charging Sessions.
 * - JDBC CRUD operations.
 */
public class SessionDAO implements GenericDAO<ChargingSession> {

    @Override
    public boolean insert(ChargingSession s) throws SQLException {
        String sql = """
            INSERT INTO charging_sessions (booking_id, user_id, slot_id, start_time, end_time, energy_delivered, total_cost, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getBookingId());
            ps.setInt(2, s.getUserId());
            ps.setInt(3, s.getSlotId());
            ps.setString(4, s.getStartTime());
            ps.setString(5, s.getEndTime());
            ps.setDouble(6, s.getEnergyDeliveredKWh());
            ps.setDouble(7, s.getTotalCost());
            ps.setString(8, s.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        s.setSessionId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean update(ChargingSession s) throws SQLException {
        String sql = """
            UPDATE charging_sessions SET end_time = ?, energy_delivered = ?, total_cost = ?, status = ?
            WHERE session_id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getEndTime());
            ps.setDouble(2, s.getEnergyDeliveredKWh());
            ps.setDouble(3, s.getTotalCost());
            ps.setString(4, s.getStatus());
            ps.setInt(5, s.getSessionId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Completes a charging session atomically:
     * - updates session to COMPLETED
     * - updates booking to COMPLETED
     * - updates slot to AVAILABLE
     * - recalculates station available slots
     */
    public boolean completeSessionAtomic(ChargingSession s, int stationId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Update session
            String updateSession = """
                UPDATE charging_sessions SET end_time = ?, energy_delivered = ?, total_cost = ?, status = 'COMPLETED'
                WHERE session_id = ?
            """;
            try (PreparedStatement ps = conn.prepareStatement(updateSession)) {
                ps.setString(1, s.getEndTime());
                ps.setDouble(2, s.getEnergyDeliveredKWh());
                ps.setDouble(3, s.getTotalCost());
                ps.setInt(4, s.getSessionId());
                ps.executeUpdate();
            }

            // 2. Update booking
            String updateBooking = "UPDATE bookings SET status = 'COMPLETED' WHERE booking_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateBooking)) {
                ps.setInt(1, s.getBookingId());
                ps.executeUpdate();
            }

            // 3. Mark slot available
            String updateSlot = "UPDATE charging_slots SET status = 'AVAILABLE' WHERE slot_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSlot)) {
                ps.setInt(1, s.getSlotId());
                ps.executeUpdate();
            }

            // 4. Update station count automatically
            if (stationId <= 0) {
                String findStnSql = "SELECT station_id FROM charging_slots WHERE slot_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(findStnSql)) {
                    ps.setInt(1, s.getSlotId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            stationId = rs.getInt(1);
                        }
                    }
                }
            }

            if (stationId > 0) {
                String updateStn = """
                    UPDATE charging_stations 
                    SET available_slots = (SELECT COUNT(*) FROM charging_slots WHERE station_id = ? AND status = 'AVAILABLE')
                    WHERE station_id = ?
                """;
                try (PreparedStatement ps = conn.prepareStatement(updateStn)) {
                    ps.setInt(1, stationId);
                    ps.setInt(2, stationId);
                    ps.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
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

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM charging_sessions WHERE session_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public ChargingSession findById(int id) throws SQLException {
        String sql = "SELECT * FROM charging_sessions WHERE session_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSession(rs);
                }
            }
        }
        return null;
    }

    public List<ChargingSession> findByUserId(int userId) throws SQLException {
        List<ChargingSession> list = new ArrayList<>();
        String sql = "SELECT * FROM charging_sessions WHERE user_id = ? ORDER BY session_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToSession(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<ChargingSession> findAll() throws SQLException {
        List<ChargingSession> list = new ArrayList<>();
        String sql = "SELECT * FROM charging_sessions ORDER BY session_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToSession(rs));
            }
        }
        return list;
    }

    public int countActiveSessions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM charging_sessions WHERE status = 'IN_PROGRESS'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countCompletedSessions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM charging_sessions WHERE status = 'COMPLETED'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private ChargingSession mapRowToSession(ResultSet rs) throws SQLException {
        return new ChargingSession(
            rs.getInt("session_id"),
            rs.getInt("booking_id"),
            rs.getInt("user_id"),
            rs.getInt("slot_id"),
            rs.getString("start_time"),
            rs.getString("end_time"),
            rs.getDouble("energy_delivered"),
            rs.getDouble("total_cost"),
            rs.getString("status")
        );
    }
}
