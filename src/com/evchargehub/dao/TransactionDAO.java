package com.evchargehub.dao;

import com.evchargehub.database.DatabaseConnection;
import com.evchargehub.model.PaymentStatus;
import com.evchargehub.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Transactions and Billing.
 * - JDBC operations.
 */
public class TransactionDAO {

    public boolean insert(Transaction t) throws SQLException {
        String sql = """
            INSERT INTO transactions (transaction_id, session_id, user_id, amount, payment_method, payment_status, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTransactionId());
            ps.setInt(2, t.getSessionId());
            ps.setInt(3, t.getUserId());
            ps.setDouble(4, t.getAmount());
            ps.setString(5, t.getPaymentMethod());
            ps.setString(6, t.getPaymentStatus().name());
            ps.setString(7, t.getTimestamp() != null ? t.getTimestamp() : new Timestamp(System.currentTimeMillis()).toString());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(String transactionId, PaymentStatus status) throws SQLException {
        String sql = "UPDATE transactions SET payment_status = ? WHERE transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, transactionId);
            return ps.executeUpdate() > 0;
        }
    }

    public Transaction findById(String transactionId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToTransaction(rs);
                }
            }
        }
        return null;
    }

    public List<Transaction> findByUserId(int userId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTransaction(rs));
                }
            }
        }
        return list;
    }

    public List<Transaction> findAll() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToTransaction(rs));
            }
        }
        return list;
    }

    public double calculateTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(amount) FROM transactions WHERE payment_status = 'PAID'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    private Transaction mapRowToTransaction(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getString("transaction_id"),
            rs.getInt("session_id"),
            rs.getInt("user_id"),
            rs.getDouble("amount"),
            rs.getString("payment_method"),
            PaymentStatus.valueOf(rs.getString("payment_status").toUpperCase()),
            rs.getString("timestamp")
        );
    }
}
