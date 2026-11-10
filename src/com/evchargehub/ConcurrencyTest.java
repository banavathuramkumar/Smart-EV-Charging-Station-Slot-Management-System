package com.evchargehub;

import com.evchargehub.database.DatabaseInitializer;
import com.evchargehub.exceptions.BookingException;
import com.evchargehub.model.Booking;
import com.evchargehub.service.BookingService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Concurrency Test to verify thread synchronization and double-booking prevention.
 * Launches 5 simultaneous threads attempting to book the EXACT SAME slot and time slot.
 * Exactly 1 must succeed; the other 4 must be rejected with BookingException.
 */
public class ConcurrencyTest {
    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("  Testing Concurrent Slot Booking Synchronization ");
        System.out.println("==================================================");

        DatabaseInitializer.initializeDatabase();
        BookingService bookingService = new BookingService();

        final int numberOfThreads = 5;
        final CountDownLatch startGun = new CountDownLatch(1);
        final CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

        final AtomicInteger successCount = new AtomicInteger(0);
        final AtomicInteger failureCount = new AtomicInteger(0);

        // All threads target Slot 1 at Station 1 for a dedicated test date from 16:00 to 17:00
        final String bookingDate = java.time.LocalDate.now().plusDays(10).toString();
        final String startTime = "16:00";
        final double duration = 1.0;
        final int stationId = 1;
        final int slotId = 1;
        final int vehicleId = 1;

        // Clean up prior test runs for this test date so the test is 100% idempotent
        try (var conn = com.evchargehub.database.DatabaseConnection.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM bookings WHERE slot_id = 1 AND booking_date = '" + bookingDate + "'");
        }

        for (int i = 1; i <= numberOfThreads; i++) {
            final int threadNum = i;
            final int userId = 2; // Valid existing user (driver_user)
            Thread clientThread = new Thread(() -> {
                try {
                    startGun.await(); // Wait for all threads to be ready
                    Booking b = bookingService.bookSlot(userId, stationId, slotId, vehicleId, bookingDate, startTime, duration);
                    System.out.println(" -> Thread " + Thread.currentThread().getName() + " [Attempt " + threadNum + "] BOOKING SUCCESS! ID: #" + b.getBookingId());
                    successCount.incrementAndGet();
                } catch (BookingException be) {
                    System.out.println(" -> Thread " + Thread.currentThread().getName() + " [Attempt " + threadNum + "] PREVENTED: " + be.getMessage());
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println(" -> Thread " + Thread.currentThread().getName() + " unexpected error: " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            }, "User-Thread-" + i);
            clientThread.start();
        }

        // Fire start gun simultaneously
        System.out.println("Firing simultaneous booking requests from " + numberOfThreads + " concurrent threads...");
        startGun.countDown();

        finishLatch.await(); // Wait for all threads to finish

        System.out.println("\n----------------- CONCURRENCY TEST SUMMARY -----------------");
        System.out.println("Total Requests:  " + numberOfThreads);
        System.out.println("Successful Bookings: " + successCount.get() + " (Must be EXACTLY 1)");
        System.out.println("Blocked Conflicts:   " + failureCount.get() + " (Must be EXACTLY 4)");

        if (successCount.get() == 1 && failureCount.get() == (numberOfThreads - 1)) {
            System.out.println(">>> CONCURRENCY & DOUBLE-BOOKING PREVENTION VERIFICATION: PASSED! <<<");
        } else {
            System.err.println(">>> CONCURRENCY VERIFICATION FAILED! Double booking occurred! <<<");
            System.exit(1);
        }
    }
}
