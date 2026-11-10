package com.evchargehub;

import com.evchargehub.database.DatabaseInitializer;
import com.evchargehub.model.ChargingSession;
import com.evchargehub.service.AuthenticationService;
import com.evchargehub.ui.AdminDashboardView;
import com.evchargehub.ui.LoginView;
import com.evchargehub.ui.UserDashboardView;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CaptureScreenshots {

    private static void saveSnapshot(Parent root, File outputFile, int width, int height) throws Exception {
        Scene scene = root.getScene();
        if (scene == null) {
            scene = new Scene(root, width, height);
        }
        root.applyCss();
        root.layout();

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.web("#0b1120"));
        WritableImage fxImage = root.snapshot(params, null);

        int imgW = (int) fxImage.getWidth();
        int imgH = (int) fxImage.getHeight();
        BufferedImage bImage = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = fxImage.getPixelReader();
        for (int y = 0; y < imgH; y++) {
            for (int x = 0; x < imgW; x++) {
                bImage.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        ImageIO.write(bImage, "png", outputFile);
        System.out.println("Saved screenshot: " + outputFile.getName() + " (" + imgW + "x" + imgH + ")");
    }

    private static void invokeShow(Object viewObj, String methodName) throws Exception {
        Method m = viewObj.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        m.invoke(viewObj);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("  EV ChargeHub - Automated Screenshot Generator   ");
        System.out.println("==================================================");

        DatabaseInitializer.initializeDatabase();
        File ssDir = new File("screenshots");
        if (!ssDir.exists()) ssDir.mkdirs();

        CountDownLatch latch = new CountDownLatch(1);

        Platform.startup(() -> {
            try {
                AuthenticationService auth = new AuthenticationService();

                // 1. User Login & Registration (LoginView)
                System.out.println("[Capture] Generating Login & Registration Screen...");
                LoginView loginView = new LoginView();
                saveSnapshot((Parent) loginView.getView(), new File(ssDir, "08_user_login.png"), 1280, 750);

                // Log in as user for user dashboard screens
                auth.login("driver_user", "User@123");
                UserDashboardView userView = new UserDashboardView();

                // 2. User Dashboard Overview
                System.out.println("[Capture] Generating User Dashboard Overview...");
                invokeShow(userView, "showOverviewScreen");
                saveSnapshot((Parent) userView.getView(), new File(ssDir, "09_user_dashboard.png"), 1280, 750);

                // 3. Vehicle Management
                System.out.println("[Capture] Generating Vehicle Management...");
                invokeShow(userView, "showVehiclesScreen");
                saveSnapshot((Parent) userView.getView(), new File(ssDir, "10_vehicle_management.png"), 1280, 750);

                // 4. Charging Slot Selection (Station Explorer)
                System.out.println("[Capture] Generating Charging Slot Selection (Station Explorer)...");
                invokeShow(userView, "showStationsScreen");
                saveSnapshot((Parent) userView.getView(), new File(ssDir, "12_slot_selection.png"), 1280, 750);

                // 5. Slot Booking
                System.out.println("[Capture] Generating Slot Booking...");
                invokeShow(userView, "showBookingScreen");
                saveSnapshot((Parent) userView.getView(), new File(ssDir, "13_slot_booking.png"), 1280, 750);

                // 6. Charging Session & Status
                System.out.println("[Capture] Generating Charging Session & Status...");
                ChargingSession dummySession = new ChargingSession(1, 1, 2, 1, "2026-11-09 14:30:00", null, 24.5, 392.0, "IN_PROGRESS");
                Field fSession = UserDashboardView.class.getDeclaredField("activeSession");
                fSession.setAccessible(true);
                fSession.set(userView, dummySession);
                invokeShow(userView, "showChargingScreen");
                saveSnapshot((Parent) userView.getView(), new File(ssDir, "14_charging_session.png"), 1280, 750);

                // 7. Billing & Transactions
                System.out.println("[Capture] Generating Billing & Transactions...");
                invokeShow(userView, "showBillingScreen");
                saveSnapshot((Parent) userView.getView(), new File(ssDir, "15_billing_transactions.png"), 1280, 750);

                // 8. User Booking History
                System.out.println("[Capture] Generating User Booking History...");
                invokeShow(userView, "showHistoryScreen");
                saveSnapshot((Parent) userView.getView(), new File(ssDir, "17b_booking_history.png"), 1280, 750);

                // Log in as Admin for Admin dashboard screens
                auth.login("admin", "Admin@123");
                AdminDashboardView adminView = new AdminDashboardView();

                // 9. Admin Dashboard
                System.out.println("[Capture] Generating Admin Dashboard...");
                invokeShow(adminView, "showOverviewScreen");
                saveSnapshot((Parent) adminView.getView(), new File(ssDir, "16_admin_dashboard.png"), 1280, 750);

                // 10. Charging Station Management (Admin)
                System.out.println("[Capture] Generating Charging Station Management (Admin)...");
                invokeShow(adminView, "showStationsScreen");
                saveSnapshot((Parent) adminView.getView(), new File(ssDir, "11_charging_stations.png"), 1280, 750);

                // 11. Reports & Audit File Handling (Admin)
                System.out.println("[Capture] Generating Reports & File Handling (Admin)...");
                invokeShow(adminView, "showReportsScreen");
                saveSnapshot((Parent) adminView.getView(), new File(ssDir, "17_reports_file_handling.png"), 1280, 750);

                System.out.println("\nAll 11 actual application screenshots generated successfully!");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
                Platform.exit();
            }
        });

        latch.await(30, TimeUnit.SECONDS);
        System.exit(0);
    }
}
