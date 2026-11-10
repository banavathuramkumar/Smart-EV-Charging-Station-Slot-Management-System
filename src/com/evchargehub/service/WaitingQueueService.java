package com.evchargehub.service;

import com.evchargehub.collections.WaitingQueue;
import com.evchargehub.database.DatabaseConnection;
import com.evchargehub.model.QueueEntry;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service managing station waiting queues.
 * Synchronizes in-memory WaitingQueue with the SQLite waiting_queue table.
 */
public class WaitingQueueService {

    // Map of Station ID -> In-memory WaitingQueue
    private static final ConcurrentHashMap<Integer, WaitingQueue> stationQueues = new ConcurrentHashMap<>();

    private WaitingQueue getOrCreateQueue(int stationId) {
        return stationQueues.computeIfAbsent(stationId, id -> new WaitingQueue(id, 20));
    }

    /**
     * User joins the waiting queue for a station.
     */
    public boolean joinQueue(int userId, int stationId, String userName, String stationName, String userEmail) throws Exception {
        // Check if user is already in queue for this station in DB
        String checkSql = "SELECT COUNT(*) FROM waiting_queue WHERE user_id = ? AND station_id = ? AND notified = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new Exception("You are already in the waiting queue for this station.");
                }
            }
        }

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        QueueEntry entry = new QueueEntry(0, userId, stationId, userName, stationName, userEmail, now, false);

        // 1. Insert into SQLite
        String insertSql = "INSERT INTO waiting_queue (user_id, station_id, join_time, notified) VALUES (?, ?, ?, 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setInt(2, stationId);
            ps.setString(3, now);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entry.setQueueId(rs.getInt(1));
                }
            }
        }

        // 2. Enqueue into thread-safe in-memory queue
        WaitingQueue queue = getOrCreateQueue(stationId);
        return queue.enqueue(entry);
    }

    /**
     * When a slot is freed, dequeue next user and notify.
     */
    public QueueEntry notifyNextUser(int stationId) throws SQLException {
        WaitingQueue queue = getOrCreateQueue(stationId);
        QueueEntry next = queue.dequeueNext();

        if (next != null) {
            String updateSql = "UPDATE waiting_queue SET notified = 1 WHERE queue_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, next.getQueueId());
                ps.executeUpdate();
            }
        } else {
            // Check DB directly in case queue restarted
            String pollDb = "SELECT q.*, u.full_name as user_name, s.name as station_name, u.email as user_email " +
                            "FROM waiting_queue q " +
                            "JOIN users u ON q.user_id = u.id " +
                            "JOIN charging_stations s ON q.station_id = s.station_id " +
                            "WHERE q.station_id = ? AND q.notified = 0 ORDER BY q.queue_id ASC LIMIT 1";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(pollDb)) {
                ps.setInt(1, stationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        next = new QueueEntry(
                            rs.getInt("queue_id"),
                            rs.getInt("user_id"),
                            rs.getInt("station_id"),
                            rs.getString("user_name"),
                            rs.getString("station_name"),
                            rs.getString("user_email"),
                            rs.getString("join_time"),
                            true
                        );
                        String updateSql = "UPDATE waiting_queue SET notified = 1 WHERE queue_id = ?";
                        try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                            upd.setInt(1, next.getQueueId());
                            upd.executeUpdate();
                        }
                    }
                }
            }
        }

        return next;
    }

    /**
     * User leaves the queue.
     */
    public boolean leaveQueue(int userId, int stationId) throws SQLException {
        WaitingQueue queue = getOrCreateQueue(stationId);
        queue.removeUser(userId);

        String deleteSql = "DELETE FROM waiting_queue WHERE user_id = ? AND station_id = ? AND notified = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, stationId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<QueueEntry> getQueueForStation(int stationId) throws SQLException {
        List<QueueEntry> list = new ArrayList<>();
        String sql = """
            SELECT q.*, u.full_name as user_name, s.name as station_name, u.email as user_email
            FROM waiting_queue q
            JOIN users u ON q.user_id = u.id
            JOIN charging_stations s ON q.station_id = s.station_id
            WHERE q.station_id = ? AND q.notified = 0
            ORDER BY q.queue_id ASC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new QueueEntry(
                        rs.getInt("queue_id"),
                        rs.getInt("user_id"),
                        rs.getInt("station_id"),
                        rs.getString("user_name"),
                        rs.getString("station_name"),
                        rs.getString("user_email"),
                        rs.getString("join_time"),
                        false
                    ));
                }
            }
        }
        return list;
    }
}
