package com.evchargehub.dao;

import com.evchargehub.database.DatabaseConnection;
import com.evchargehub.model.Admin;
import com.evchargehub.model.Person;
import com.evchargehub.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User and Admin entities.
 * Demonstrates:
 * - JDBC CRUD operations, PreparedStatement, ResultSet
 * - Polymorphic Person hierarchy (instantiating User or Admin based on role)
 * - Exception handling with try-catch and throws
 */
public class UserDAO implements GenericDAO<Person> {

    @Override
    public boolean insert(Person person) throws SQLException {
        String sql = """
            INSERT INTO users (username, password_hash, full_name, email, phone, role, wallet_balance, driving_license, department, access_level)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, person.getUsername());
            ps.setString(2, person.getPasswordHash());
            ps.setString(3, person.getFullName());
            ps.setString(4, person.getEmail());
            ps.setString(5, person.getPhone());
            ps.setString(6, person.getRole());

            if (person instanceof User u) {
                ps.setDouble(7, u.getWalletBalance());
                ps.setString(8, u.getDrivingLicenseNumber());
                ps.setNull(9, Types.VARCHAR);
                ps.setInt(10, 1);
            } else if (person instanceof Admin a) {
                ps.setDouble(7, 0.0);
                ps.setNull(8, Types.VARCHAR);
                ps.setString(9, a.getDepartment());
                ps.setInt(10, a.getAccessLevel());
            } else {
                ps.setDouble(7, 0.0);
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.VARCHAR);
                ps.setInt(10, 1);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        person.setId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean update(Person person) throws SQLException {
        String sql = """
            UPDATE users SET full_name = ?, email = ?, phone = ?, wallet_balance = ?, driving_license = ?, department = ?, access_level = ?
            WHERE id = ?
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, person.getFullName());
            ps.setString(2, person.getEmail());
            ps.setString(3, person.getPhone());

            if (person instanceof User u) {
                ps.setDouble(4, u.getWalletBalance());
                ps.setString(5, u.getDrivingLicenseNumber());
                ps.setNull(6, Types.VARCHAR);
                ps.setInt(7, 1);
            } else if (person instanceof Admin a) {
                ps.setDouble(4, 0.0);
                ps.setNull(5, Types.VARCHAR);
                ps.setString(6, a.getDepartment());
                ps.setInt(7, a.getAccessLevel());
            } else {
                ps.setDouble(4, 0.0);
                ps.setNull(5, Types.VARCHAR);
                ps.setNull(6, Types.VARCHAR);
                ps.setInt(7, 1);
            }
            ps.setInt(8, person.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateWalletBalance(int userId, double newBalance) throws SQLException {
        String sql = "UPDATE users SET wallet_balance = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Person findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPerson(rs);
                }
            }
        }
        return null;
    }

    public Person findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPerson(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Person> findAll() throws SQLException {
        List<Person> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToPerson(rs));
            }
        }
        return list;
    }

    public int countTotalUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'USER'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Person mapRowToPerson(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String passHash = rs.getString("password_hash");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");

        if ("ADMIN".equalsIgnoreCase(role)) {
            String dept = rs.getString("department");
            int access = rs.getInt("access_level");
            return new Admin(id, username, passHash, fullName, email, phone, dept, access);
        } else {
            double balance = rs.getDouble("wallet_balance");
            User u = new User(id, username, passHash, fullName, email, phone, balance);
            u.setDrivingLicenseNumber(rs.getString("driving_license"));
            return u;
        }
    }
}
