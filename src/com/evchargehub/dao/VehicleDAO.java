package com.evchargehub.dao;

import com.evchargehub.database.DatabaseConnection;
import com.evchargehub.model.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Electric Vehicles.
 * - JDBC CRUD operations.
 */
public class VehicleDAO implements GenericDAO<Vehicle> {

    @Override
    public boolean insert(Vehicle v) throws SQLException {
        String sql = """
            INSERT INTO vehicles (owner_id, reg_number, brand, model, battery_capacity, connector_type)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getOwnerId());
            ps.setString(2, v.getRegistrationNumber().toUpperCase().trim());
            ps.setString(3, v.getBrand());
            ps.setString(4, v.getModel());
            ps.setDouble(5, v.getBatteryCapacityKWh());
            ps.setString(6, v.getConnectorType());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        v.setVehicleId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean update(Vehicle v) throws SQLException {
        String sql = """
            UPDATE vehicles SET reg_number = ?, brand = ?, model = ?, battery_capacity = ?, connector_type = ?
            WHERE vehicle_id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getRegistrationNumber().toUpperCase().trim());
            ps.setString(2, v.getBrand());
            ps.setString(3, v.getModel());
            ps.setDouble(4, v.getBatteryCapacityKWh());
            ps.setString(5, v.getConnectorType());
            ps.setInt(6, v.getVehicleId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Vehicle findById(int id) throws SQLException {
        String sql = "SELECT * FROM vehicles WHERE vehicle_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToVehicle(rs);
                }
            }
        }
        return null;
    }

    public List<Vehicle> findByOwnerId(int ownerId) throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicles WHERE owner_id = ? ORDER BY vehicle_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToVehicle(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Vehicle> findAll() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT * FROM vehicles ORDER BY vehicle_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToVehicle(rs));
            }
        }
        return list;
    }

    public int countTotalVehicles() throws SQLException {
        String sql = "SELECT COUNT(*) FROM vehicles";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Vehicle mapRowToVehicle(ResultSet rs) throws SQLException {
        return new Vehicle(
            rs.getInt("vehicle_id"),
            rs.getInt("owner_id"),
            rs.getString("reg_number"),
            rs.getString("brand"),
            rs.getString("model"),
            rs.getDouble("battery_capacity"),
            rs.getString("connector_type")
        );
    }
}
