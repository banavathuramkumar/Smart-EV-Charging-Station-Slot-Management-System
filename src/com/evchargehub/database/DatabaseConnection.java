package com.evchargehub.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages SQLite database connectivity using standard JDBC.
 * - SQLite database connectivity
 * - JDBC DriverManager and Connection
 * - Database file persistence
 */
public class DatabaseConnection {

    private static final String DB_DIR = "data";
    private static final String DB_FILE = DB_DIR + File.separator + "ev_chargehub.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE;

    static {
        try {
            // Ensure data directory exists
            File dir = new File(DB_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            // Load SQLite JDBC Driver class
            Class.forName("org.sqlite.JDBC");
            System.out.println("[DatabaseConnection] SQLite JDBC Driver successfully loaded.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] SQLite JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    /**
     * Obtains a new database connection.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(JDBC_URL);
        // Enable Foreign Key support in SQLite
        try (var stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static String getDatabasePath() {
        return new File(DB_FILE).getAbsolutePath();
    }
}
