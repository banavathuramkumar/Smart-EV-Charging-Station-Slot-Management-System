package com.evchargehub.database;

import com.evchargehub.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Responsible for creating SQLite tables on application start and seeding initial data.
 * - Table creation and schema management via JDBC
 * - Safe transaction handling and resource management
 * - PreparedStatement and SQL execution
 */
public class DatabaseInitializer {

    public static void initializeDatabase() {
        System.out.println("[DatabaseInitializer] Initializing SQLite database schema...");

        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                full_name TEXT NOT NULL,
                email TEXT,
                phone TEXT,
                role TEXT NOT NULL DEFAULT 'USER',
                wallet_balance REAL DEFAULT 100.0,
                driving_license TEXT,
                department TEXT,
                access_level INTEGER DEFAULT 1,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String createVehiclesTable = """
            CREATE TABLE IF NOT EXISTS vehicles (
                vehicle_id INTEGER PRIMARY KEY AUTOINCREMENT,
                owner_id INTEGER NOT NULL,
                reg_number TEXT UNIQUE NOT NULL,
                brand TEXT NOT NULL,
                model TEXT NOT NULL,
                battery_capacity REAL NOT NULL,
                connector_type TEXT NOT NULL,
                FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
            );
        """;

        String createStationsTable = """
            CREATE TABLE IF NOT EXISTS charging_stations (
                station_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                location TEXT NOT NULL,
                total_slots INTEGER NOT NULL,
                available_slots INTEGER NOT NULL,
                charging_speed REAL NOT NULL,
                price_per_kwh REAL NOT NULL,
                status TEXT NOT NULL DEFAULT 'ACTIVE'
            );
        """;

        String createSlotsTable = """
            CREATE TABLE IF NOT EXISTS charging_slots (
                slot_id INTEGER PRIMARY KEY AUTOINCREMENT,
                station_id INTEGER NOT NULL,
                slot_number TEXT NOT NULL,
                connector_type TEXT NOT NULL,
                charging_power REAL NOT NULL,
                status TEXT NOT NULL DEFAULT 'AVAILABLE',
                FOREIGN KEY (station_id) REFERENCES charging_stations(station_id) ON DELETE CASCADE,
                UNIQUE (station_id, slot_number)
            );
        """;

        String createBookingsTable = """
            CREATE TABLE IF NOT EXISTS bookings (
                booking_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                station_id INTEGER NOT NULL,
                slot_id INTEGER NOT NULL,
                vehicle_id INTEGER NOT NULL,
                booking_date TEXT NOT NULL,
                start_time TEXT NOT NULL,
                duration_hours REAL NOT NULL,
                end_time TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'CONFIRMED',
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (station_id) REFERENCES charging_stations(station_id),
                FOREIGN KEY (slot_id) REFERENCES charging_slots(slot_id),
                FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id)
            );
        """;

        String createSessionsTable = """
            CREATE TABLE IF NOT EXISTS charging_sessions (
                session_id INTEGER PRIMARY KEY AUTOINCREMENT,
                booking_id INTEGER NOT NULL,
                user_id INTEGER NOT NULL,
                slot_id INTEGER NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT,
                energy_delivered REAL DEFAULT 0.0,
                total_cost REAL DEFAULT 0.0,
                status TEXT NOT NULL DEFAULT 'IN_PROGRESS',
                FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (slot_id) REFERENCES charging_slots(slot_id)
            );
        """;

        String createTransactionsTable = """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id TEXT PRIMARY KEY,
                session_id INTEGER NOT NULL,
                user_id INTEGER NOT NULL,
                amount REAL NOT NULL,
                payment_method TEXT NOT NULL,
                payment_status TEXT NOT NULL DEFAULT 'PAID',
                timestamp TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (session_id) REFERENCES charging_sessions(session_id),
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
        """;

        String createQueueTable = """
            CREATE TABLE IF NOT EXISTS waiting_queue (
                queue_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                station_id INTEGER NOT NULL,
                join_time TEXT DEFAULT CURRENT_TIMESTAMP,
                notified INTEGER DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (station_id) REFERENCES charging_stations(station_id)
            );
        """;

        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.createStatement();

            // Execute table creations in sequence
            stmt.execute(createUsersTable);
            stmt.execute(createVehiclesTable);
            stmt.execute(createStationsTable);
            stmt.execute(createSlotsTable);
            stmt.execute(createBookingsTable);
            stmt.execute(createSessionsTable);
            stmt.execute(createTransactionsTable);
            stmt.execute(createQueueTable);

            System.out.println("[DatabaseInitializer] All 8 tables successfully checked/created.");

            // Seed initial records if empty
            seedInitialData(conn);

        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] SQL Error during initialization: " + e.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close statement: " + e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    private static void seedInitialData(Connection conn) {
        try {
            // Check if admin user exists
            String checkAdmin = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(checkAdmin)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // Seed initial Admin (Password: Admin@123)
                    String insertAdmin = """
                        INSERT INTO users (username, password_hash, full_name, email, phone, role, department, access_level)
                        VALUES (?, ?, ?, ?, ?, 'ADMIN', 'Station Operations', 5)
                    """;
                    try (PreparedStatement ps = conn.prepareStatement(insertAdmin)) {
                        ps.setString(1, "admin");
                        ps.setString(2, PasswordUtil.hashPassword("Admin@123"));
                        ps.setString(3, "System Administrator");
                        ps.setString(4, "admin@evchargehub.com");
                        ps.setString(5, "9876543210");
                        ps.executeUpdate();
                        System.out.println("[DatabaseInitializer] Seeded initial Admin account (username: admin)");
                    }
                }
            }

            // Check if users exist
            String checkUser = "SELECT COUNT(*) FROM users WHERE role = 'USER'";
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(checkUser)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // Seed Primary Driver User (Password: User@123)
                    String insertUser1 = """
                        INSERT INTO users (username, password_hash, full_name, email, phone, role, wallet_balance, driving_license)
                        VALUES (?, ?, ?, ?, ?, 'USER', 150.0, 'DL-AP-2024-8841')
                    """;
                    int driverId1 = -1;
                    try (PreparedStatement ps = conn.prepareStatement(insertUser1, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, "driver_user");
                        ps.setString(2, PasswordUtil.hashPassword("User@123"));
                        ps.setString(3, "Rahul Sharma");
                        ps.setString(4, "rahul.sharma@example.com");
                        ps.setString(5, "9123456780");
                        ps.executeUpdate();
                        ResultSet keys = ps.getGeneratedKeys();
                        if (keys.next()) {
                            driverId1 = keys.getInt(1);
                        }
                        System.out.println("[DatabaseInitializer] Seeded driver account (username: driver_user)");
                    }

                    if (driverId1 > 0) {
                        String insertVehicle = """
                            INSERT INTO vehicles (owner_id, reg_number, brand, model, battery_capacity, connector_type)
                            VALUES (?, 'AP 16 EV 9090', 'Tata', 'Nexon EV Max', 40.5, 'CCS2')
                        """;
                        try (PreparedStatement ps = conn.prepareStatement(insertVehicle)) {
                            ps.setInt(1, driverId1);
                            ps.executeUpdate();
                        }
                    }

                    // Seed Secondary Driver User (Password: User@123)
                    String insertUser2 = """
                        INSERT INTO users (username, password_hash, full_name, email, phone, role, wallet_balance, driving_license)
                        VALUES (?, ?, ?, ?, ?, 'USER', 180.0, 'DL-TS-2023-4412')
                    """;
                    int driverId2 = -1;
                    try (PreparedStatement ps = conn.prepareStatement(insertUser2, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, "priya_reddy");
                        ps.setString(2, PasswordUtil.hashPassword("User@123"));
                        ps.setString(3, "Priya Reddy");
                        ps.setString(4, "priya.reddy@example.com");
                        ps.setString(5, "9876543211");
                        ps.executeUpdate();
                        ResultSet keys = ps.getGeneratedKeys();
                        if (keys.next()) {
                            driverId2 = keys.getInt(1);
                        }
                        System.out.println("[DatabaseInitializer] Seeded driver account (username: priya_reddy)");
                    }

                    if (driverId2 > 0) {
                        String insertVehicle2 = """
                            INSERT INTO vehicles (owner_id, reg_number, brand, model, battery_capacity, connector_type)
                            VALUES (?, 'TS 09 EV 4040', 'MG', 'ZS EV Long Range', 50.3, 'CCS2')
                        """;
                        try (PreparedStatement ps = conn.prepareStatement(insertVehicle2)) {
                            ps.setInt(1, driverId2);
                            ps.executeUpdate();
                        }
                    }
                }
            }

            // Check if charging stations exist
            String checkStations = "SELECT COUNT(*) FROM charging_stations";
            try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(checkStations)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // Seed Vijayawada to Hyderabad Highway Network (NH 65 Corridor)

                    // 1. Vijayawada Benz Circle
                    insertStationWithSlots(conn, "Benz Circle SuperCharge Hub", "MG Road, Vijayawada", 150.0, 16.5, new String[][]{
                        {"SLOT-V1", "CCS2", "150.0"},
                        {"SLOT-V2", "CCS2", "150.0"},
                        {"SLOT-V3", "Type 2", "22.0"},
                        {"SLOT-V4", "CHAdeMO", "60.0"}
                    });

                    // 2. Ibrahimpatnam Junction
                    insertStationWithSlots(conn, "Ibrahimpatnam Highway Fast Hub", "NH 65 Junction, Ibrahimpatnam", 120.0, 15.0, new String[][]{
                        {"SLOT-IB1", "CCS2", "120.0"},
                        {"SLOT-IB2", "CCS2", "120.0"},
                        {"SLOT-IB3", "Type 2", "22.0"}
                    });

                    // 3. Nandigama Bypass
                    insertStationWithSlots(conn, "Nandigama Express EV Stop", "NH 65 Bypass, Nandigama", 60.0, 14.0, new String[][]{
                        {"SLOT-ND1", "CCS2", "60.0"},
                        {"SLOT-ND2", "GB/T", "60.0"},
                        {"SLOT-ND3", "Type 2", "22.0"}
                    });

                    // 4. Kodad Border Oasis
                    insertStationWithSlots(conn, "Kodad Highway EV Oasis", "AP-Telangana Border, NH 65, Kodad", 120.0, 15.5, new String[][]{
                        {"SLOT-KD1", "CCS2", "120.0"},
                        {"SLOT-KD2", "CCS2", "120.0"},
                        {"SLOT-KD3", "Type 2", "22.0"},
                        {"SLOT-KD4", "CHAdeMO", "50.0"}
                    });

                    // 5. Suryapet Midway SuperHub
                    insertStationWithSlots(conn, "Suryapet Highway Midway SuperHub", "NH 65 Food Court Plaza, Suryapet", 240.0, 17.0, new String[][]{
                        {"SLOT-SP1", "CCS2", "240.0"},
                        {"SLOT-SP2", "CCS2", "240.0"},
                        {"SLOT-SP3", "CCS2", "120.0"},
                        {"SLOT-SP4", "Type 2", "22.0"},
                        {"SLOT-SP5", "CHAdeMO", "60.0"}
                    });

                    // 6. Nakrekal Point
                    insertStationWithSlots(conn, "Nakrekal Express EV Point", "NH 65, Nakrekal, Nalgonda Dist", 60.0, 14.5, new String[][]{
                        {"SLOT-NK1", "CCS2", "60.0"},
                        {"SLOT-NK2", "Type 2", "22.0"}
                    });

                    // 7. Narketpally Junction
                    insertStationWithSlots(conn, "Narketpally Highway Fast Charger", "NH 65 Junction, Narketpally", 120.0, 15.5, new String[][]{
                        {"SLOT-NP1", "CCS2", "120.0"},
                        {"SLOT-NP2", "CCS2", "120.0"},
                        {"SLOT-NP3", "Type 2", "22.0"}
                    });

                    // 8. Choutuppal Toll Plaza
                    insertStationWithSlots(conn, "Choutuppal Green Highway Station", "NH 65 Toll Plaza, Choutuppal", 150.0, 16.0, new String[][]{
                        {"SLOT-CH1", "CCS2", "150.0"},
                        {"SLOT-CH2", "CCS2", "150.0"},
                        {"SLOT-CH3", "Type 2", "22.0"},
                        {"SLOT-CH4", "GB/T", "60.0"}
                    });

                    // 9. LB Nagar Transit Hub (Hyderabad Entry)
                    insertStationWithSlots(conn, "LB Nagar Express EV Transit Hub", "Inner Ring Road, LB Nagar, Hyderabad", 150.0, 17.5, new String[][]{
                        {"SLOT-LB1", "CCS2", "150.0"},
                        {"SLOT-LB2", "CCS2", "150.0"},
                        {"SLOT-LB3", "Type 2", "22.0"},
                        {"SLOT-LB4", "CHAdeMO", "60.0"}
                    });

                    // 10. Gachibowli Financial District
                    insertStationWithSlots(conn, "Gachibowli Tech Park SuperCharge", "Financial District, Gachibowli, Hyderabad", 180.0, 18.5, new String[][]{
                        {"SLOT-GB1", "CCS2", "180.0"},
                        {"SLOT-GB2", "CCS2", "180.0"},
                        {"SLOT-GB3", "Type 2", "22.0"},
                        {"SLOT-GB4", "Type 2", "22.0"}
                    });

                    // 11. HITEC City Cyber Towers
                    insertStationWithSlots(conn, "HITEC City Cyber Towers EV Hub", "Madhapur, HITEC City, Hyderabad", 240.0, 19.0, new String[][]{
                        {"SLOT-HC1", "CCS2", "240.0"},
                        {"SLOT-HC2", "CCS2", "240.0"},
                        {"SLOT-HC3", "CCS2", "120.0"},
                        {"SLOT-HC4", "Type 2", "22.0"}
                    });

                    // 12. Shamshabad Airport
                    insertStationWithSlots(conn, "Shamshabad Airport EV Express Hub", "RGIA Approach Road, Shamshabad, Hyderabad", 180.0, 18.0, new String[][]{
                        {"SLOT-SH1", "CCS2", "180.0"},
                        {"SLOT-SH2", "CCS2", "180.0"},
                        {"SLOT-SH3", "Type 2", "22.0"},
                        {"SLOT-SH4", "CHAdeMO", "60.0"}
                    });

                    System.out.println("[DatabaseInitializer] Seeded 12 Vijayawada-to-Hyderabad highway charging stations with slots.");
                }
            }

        } catch (SQLException e) {
            System.err.println("[DatabaseInitializer] Error seeding initial data: " + e.getMessage());
        }
    }

    private static void insertStationWithSlots(Connection conn, String name, String location, double speed, double price, String[][] slots) throws SQLException {
        String insertStn = """
            INSERT INTO charging_stations (name, location, total_slots, available_slots, charging_speed, price_per_kwh, status)
            VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')
        """;
        int stationId = 0;
        try (PreparedStatement ps = conn.prepareStatement(insertStn, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, location);
            ps.setInt(3, slots.length);
            ps.setInt(4, slots.length);
            ps.setDouble(5, speed);
            ps.setDouble(6, price);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                stationId = rs.getInt(1);
            }
        }

        if (stationId > 0) {
            String insertSlot = """
                INSERT INTO charging_slots (station_id, slot_number, connector_type, charging_power, status)
                VALUES (?, ?, ?, ?, 'AVAILABLE')
            """;
            try (PreparedStatement ps = conn.prepareStatement(insertSlot)) {
                for (String[] slot : slots) {
                    ps.setInt(1, stationId);
                    ps.setString(2, slot[0]);
                    ps.setString(3, slot[1]);
                    ps.setDouble(4, Double.parseDouble(slot[2]));
                    ps.executeUpdate();
                }
            }
        }
    }
}
