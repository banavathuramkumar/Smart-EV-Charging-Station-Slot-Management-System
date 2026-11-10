package com.evchargehub.util;

import com.evchargehub.model.Booking;
import com.evchargehub.model.ChargingStation;
import com.evchargehub.model.Transaction;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Report generation and file handling utility.
 * Demonstrates:
 * - Stream classes and Byte Streams
 * - InputStream and OutputStream
 * - FileInputStream and FileOutputStream
 * - Nested try-catch statements and finally blocks
 */
public class ReportGenerator {

    private static final String REPORTS_DIR = "reports";

    static {
        File dir = new File(REPORTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Exports a Booking Report to a CSV file using FileOutputStream (Byte Stream).
     * Demonstrates:
     * - OutputStream and FileOutputStream
     * - Nested try statements
     * - finally block
     */
    public static File generateBookingReport(List<Booking> bookings) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File file = new File(REPORTS_DIR, "bookings_report_" + timestamp + ".csv");

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);

            StringBuilder sb = new StringBuilder();
            sb.append("Booking ID,User Name,Station Name,Slot Number,Vehicle Info,Date,Start Time,End Time,Duration (Hrs),Status,Created At\n");

            for (Booking b : bookings) {
                sb.append(b.getBookingId()).append(",")
                  .append("\"").append(b.getUserName() != null ? b.getUserName() : "User #" + b.getUserId()).append("\",")
                  .append("\"").append(b.getStationName() != null ? b.getStationName() : "Station #" + b.getStationId()).append("\",")
                  .append("\"").append(b.getSlotNumber() != null ? b.getSlotNumber() : "Slot #" + b.getSlotId()).append("\",")
                  .append("\"").append(b.getVehicleInfo() != null ? b.getVehicleInfo() : "Vehicle #" + b.getVehicleId()).append("\",")
                  .append(b.getBookingDate()).append(",")
                  .append(b.getStartTime()).append(",")
                  .append(b.getEndTime()).append(",")
                  .append(b.getDurationHours()).append(",")
                  .append(b.getStatus()).append(",")
                  .append(b.getCreatedAt() != null ? b.getCreatedAt() : "").append("\n");
            }

            // Write raw bytes using Byte Stream OutputStream
            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            outputStream.write(bytes);
            outputStream.flush();

        } finally {
            // Safe finally block with nested try for stream closing
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    System.err.println("Failed to close FileOutputStream: " + ex.getMessage());
                }
            }
        }

        return file;
    }

    /**
     * Exports a Financial Revenue Report using FileOutputStream.
     */
    public static File generateRevenueReport(List<Transaction> transactions, double totalRevenue) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File file = new File(REPORTS_DIR, "revenue_report_" + timestamp + ".csv");

        try (OutputStream outputStream = new FileOutputStream(file)) {
            StringBuilder sb = new StringBuilder();
            sb.append("EV ChargeHub - Revenue Summary Report\n");
            sb.append("Generated On,").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
            sb.append("Total Revenue Generated ($),").append(String.format("%.2f", totalRevenue)).append("\n\n");
            sb.append("Transaction ID,Session ID,User ID,Amount ($),Payment Method,Payment Status,Timestamp\n");

            for (Transaction t : transactions) {
                sb.append(t.getTransactionId()).append(",")
                  .append(t.getSessionId()).append(",")
                  .append(t.getUserId()).append(",")
                  .append(String.format("%.2f", t.getAmount())).append(",")
                  .append(t.getPaymentMethod()).append(",")
                  .append(t.getPaymentStatus()).append(",")
                  .append(t.getTimestamp()).append("\n");
            }

            outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }

        return file;
    }

    /**
     * Exports a Station Utilization and Status Report.
     */
    public static File generateStationReport(List<ChargingStation> stations) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File file = new File(REPORTS_DIR, "station_utilization_" + timestamp + ".txt");

        try (OutputStream outputStream = new FileOutputStream(file)) {
            StringBuilder sb = new StringBuilder();
            sb.append("========================================================================\n");
            sb.append("       EV CHARGEHUB - CHARGING STATION UTILIZATION REPORT              \n");
            sb.append("========================================================================\n");
            sb.append("Report Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"))).append("\n");
            sb.append(String.format("%-6s | %-24s | %-16s | %-6s | %-6s | %-8s | %-10s | %-8s\n",
                    "ID", "Station Name", "Location", "Total", "Avail", "Power", "Price/kWh", "Status"));
            sb.append("------------------------------------------------------------------------\n");

            for (ChargingStation s : stations) {
                sb.append(String.format("%-6d | %-24s | %-16s | %-6d | %-6d | %-6.1f kW | $%-9.2f | %-8s\n",
                        s.getStationId(), s.getName(), s.getLocation(), s.getTotalSlots(), s.getAvailableSlots(),
                        s.getChargingSpeedKW(), s.getPricePerKWh(), s.getStatus()));
            }

            sb.append("========================================================================\n");
            outputStream.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }

        return file;
    }

    /**
     * Reads a report file back into a String using FileInputStream (Byte Stream).
     * Demonstrates:
     * - InputStream and FileInputStream
     * - Reading bytes into byte buffer
     */
    public static String readReportContent(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("Report file does not exist: " + file.getAbsolutePath());
        }

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toString(StandardCharsets.UTF_8.name());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.err.println("Failed to close FileInputStream: " + e.getMessage());
                }
            }
        }
    }
}
