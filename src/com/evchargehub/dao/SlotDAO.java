package com.evchargehub.dao;

import com.evchargehub.database.DatabaseConnection;
import com.evchargehub.model.ChargingSlot;
import com.evchargehub.model.SlotStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Charging Slots.
 * - JDBC slot management operations.
 */
public class SlotDAO implements GenericDAO<ChargingSlot> {

    @Override
    public boolean insert(ChargingSlot slot) throws SQLException {
        String sql = """
            INSERT INTO charging_slots (station_id, slot_number, connector_type, charging_power, status)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, slot.getStationId());
            ps.setString(2, slot.getSlotNumber().trim().toUpperCase());
            ps.setString(3, slot.getConnectorType());
            ps.setDouble(4, slot.getChargingPowerKW());
            ps.setString(5, slot.getStatus().name());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        slot.setSlotId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean update(ChargingSlot slot) throws SQLException {
        String sql = """
            UPDATE charging_slots SET slot_number = ?, connector_type = ?, charging_power = ?, status = ?
            WHERE slot_id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slot.getSlotNumber().trim().toUpperCase());
            ps.setString(2, slot.getConnectorType());
            ps.setDouble(3, slot.getChargingPowerKW());
            ps.setString(4, slot.getStatus().name());
            ps.setInt(5, slot.getSlotId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int slotId, SlotStatus status) throws SQLException {
        String sql = "UPDATE charging_slots SET status = ? WHERE slot_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, slotId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM charging_slots WHERE slot_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public ChargingSlot findById(int id) throws SQLException {
        String sql = "SELECT * FROM charging_slots WHERE slot_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSlot(rs);
                }
            }
        }
        return null;
    }

    public List<ChargingSlot> findByStationId(int stationId) throws SQLException {
        List<ChargingSlot> list = new ArrayList<>();
        String sql = "SELECT * FROM charging_slots WHERE station_id = ? ORDER BY slot_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToSlot(rs));
                }
            }
        }
        return list;
    }

    public List<ChargingSlot> findAvailableByStationId(int stationId) throws SQLException {
        List<ChargingSlot> list = new ArrayList<>();
        String sql = "SELECT * FROM charging_slots WHERE station_id = ? AND status = 'AVAILABLE' ORDER BY slot_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToSlot(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<ChargingSlot> findAll() throws SQLException {
        List<ChargingSlot> list = new ArrayList<>();
        String sql = "SELECT * FROM charging_slots ORDER BY slot_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToSlot(rs));
            }
        }
        return list;
    }

    public int countTotalSlots() throws SQLException {
        String sql = "SELECT COUNT(*) FROM charging_slots";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countAvailableSlots() throws SQLException {
        String sql = "SELECT COUNT(*) FROM charging_slots WHERE status = 'AVAILABLE'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private ChargingSlot mapRowToSlot(ResultSet rs) throws SQLException {
        return new ChargingSlot(
            rs.getInt("slot_id"),
            rs.getInt("station_id"),
            rs.getString("slot_number"),
            rs.getString("connector_type"),
            rs.getDouble("charging_power"),
            SlotStatus.valueOf(rs.getString("status").toUpperCase())
        );
    }
}
