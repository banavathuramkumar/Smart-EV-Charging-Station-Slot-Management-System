package com.evchargehub;

import com.evchargehub.collections.CollectionsDemo;
import com.evchargehub.database.DatabaseConnection;
import com.evchargehub.database.DatabaseInitializer;
import com.evchargehub.events.AWTEventAdapterBridge;
import com.evchargehub.model.Person;
import com.evchargehub.service.AuthenticationService;
import com.evchargehub.service.BookingService;
import com.evchargehub.service.StationService;
import com.evchargehub.util.ReportGenerator;

import java.io.File;
import java.util.List;

public class BackendTest {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  EV ChargeHub - Backend & Core Java Verification ");
        System.out.println("==================================================");

        try {
            // 1. Initialize SQLite Database
            System.out.println("\n[1] Initializing SQLite Database...");
            DatabaseInitializer.initializeDatabase();
            System.out.println("    DB Location: " + DatabaseConnection.getDatabasePath());

            // 2. Test Authentication
            System.out.println("\n[2] Testing Authentication...");
            AuthenticationService auth = new AuthenticationService();
            Person admin = auth.login("admin", "Admin@123");
            System.out.println("    Admin Login: SUCCESS -> " + admin.getRoleDescription());

            Person user = auth.login("driver_user", "User@123");
            System.out.println("    User Login:  SUCCESS -> " + user.getRoleDescription());

            // 3. Test Stations and Slots Retrieval
            System.out.println("\n[3] Testing Station & Slot Services...");
            StationService stationService = new StationService();
            var stations = stationService.getAllStations();
            System.out.println("    Loaded Stations count: " + stations.size());
            for (var stn : stations) {
                var slots = stationService.getSlotsByStation(stn.getStationId());
                System.out.println("    - " + stn.getName() + " (" + stn.getLocation() + ") -> Slots: " + slots.size());
            }

            // 4. Test Collections Demo (Verification)
            System.out.println("\n[4] Running Collections Demonstration...");
            CollectionsDemo.main(new String[0]);

            // 5. Test Event Delegation Model (Verification)
            System.out.println("\n[5] Running AWT Event Delegation Demonstration...");
            AWTEventAdapterBridge.testDelegationModel();

            // 6. Test Report Generation (Java I/O Byte Streams Verification)
            System.out.println("\n[6] Testing Report Generation (Byte Streams & Files)...");
            BookingService bookingService = new BookingService();
            var bookings = bookingService.getAllBookings();
            File reportFile = ReportGenerator.generateStationReport(stations);
            System.out.println("    Generated Station Report at: " + reportFile.getAbsolutePath());
            String reportText = ReportGenerator.readReportContent(reportFile);
            System.out.println("    Read back bytes from report (length " + reportText.length() + " chars). First 100 chars: " + reportText.substring(0, Math.min(100, reportText.length())));

            System.out.println("\n==================================================");
            System.out.println("  ALL BACKEND INTEGRATION TESTS PASSED 100%!      ");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("Backend test failed!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
