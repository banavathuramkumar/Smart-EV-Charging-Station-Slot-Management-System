package com.evchargehub.dao;

import com.evchargehub.database.DatabaseConnection;
import com.evchargehub.model.ChargingStation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Charging Stations.
 * - JDBC station management operations.
 */
public class StationDAO implements GenericDAO<ChargingStation> {

    @Override
    public boolean insert(ChargingStation s) throws SQLException {
        String sql = """
            INSERT INTO charging_stations (name, location, total_slots, available_slots, charging_speed, price_per_kwh, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getLocation());
            ps.setInt(3, s.getTotalSlots());
            ps.setInt(4, s.getAvailableSlots());
            ps.setDouble(5, s.getChargingSpeedKW());
            ps.setDouble(6, s.getPricePerKWh());
            ps.setString(7, s.getStatus());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        s.setStationId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean update(ChargingStation s) throws SQLException {
        String sql = """
            UPDATE charging_stations SET name = ?, location = ?, total_slots = ?, available_slots = ?, charging_speed = ?, price_per_kwh = ?, status = ?
            WHERE station_id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getLocation());
            ps.setInt(3, s.getTotalSlots());
            ps.setInt(4, s.getAvailableSlots());
            ps.setDouble(5, s.getChargingSpeedKW());
            ps.setDouble(6, s.getPricePerKWh());
            ps.setString(7, s.getStatus());
            ps.setInt(8, s.getStationId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int stationId, String status) throws SQLException {
        String sql = "UPDATE charging_stations SET status = ? WHERE station_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, stationId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean recalculateAvailableSlots(int stationId) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM charging_slots WHERE station_id = ? AND status = 'AVAILABLE'";
        String totalSql = "SELECT COUNT(*) FROM charging_slots WHERE station_id = ?";
        String updateSql = "UPDATE charging_stations SET available_slots = ?, total_slots = ? WHERE station_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            int avail = 0;
            int total = 0;
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                ps.setInt(1, stationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) avail = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(totalSql)) {
                ps.setInt(1, stationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) total = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, avail);
                ps.setInt(2, total);
                ps.setInt(3, stationId);
                return ps.executeUpdate() > 0;
            }
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM charging_stations WHERE station_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public ChargingStation findById(int id) throws SQLException {
        String sql = "SELECT * FROM charging_stations WHERE station_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStation(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<ChargingStation> findAll() throws SQLException {
        List<ChargingStation> list = new ArrayList<>();
        String sql = "SELECT * FROM charging_stations ORDER BY station_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToStation(rs));
            }
        }
        return list;
    }

    public List<ChargingStation> findActiveStations() throws SQLException {
        List<ChargingStation> list = new ArrayList<>();
        String sql = "SELECT * FROM charging_stations WHERE status = 'ACTIVE' ORDER BY station_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToStation(rs));
            }
        }
        return list;
    }

    public int countTotalStations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM charging_stations";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private ChargingStation mapRowToStation(ResultSet rs) throws SQLException {
        return new ChargingStation(
            rs.getInt("station_id"),
            rs.getString("name"),
            rs.getString("location"),
            rs.getInt("total_slots"),
            rs.getInt("available_slots"),
            rs.getDouble("charging_speed"),
            rs.getDouble("price_per_kwh"),
            rs.getString("status")
        );
    }
}
