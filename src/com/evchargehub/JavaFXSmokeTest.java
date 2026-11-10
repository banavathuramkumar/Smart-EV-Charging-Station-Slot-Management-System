package com.evchargehub;

import com.evchargehub.database.DatabaseInitializer;
import com.evchargehub.model.Admin;
import com.evchargehub.model.User;
import com.evchargehub.service.AuthenticationService;
import com.evchargehub.ui.AdminDashboardView;
import com.evchargehub.ui.LoginView;
import com.evchargehub.ui.UserDashboardView;
import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Headless Smoke Test that instantiates JavaFX Platform and all major view controllers
 * to guarantee that all UI layouts, buttons, tables, and event bindings initialize without crashing.
 */
public class JavaFXSmokeTest {

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("  EV ChargeHub - JavaFX UI Smoke & Component Test ");
        System.out.println("==================================================");

        DatabaseInitializer.initializeDatabase();

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] success = {false};

        // Initialize JavaFX Toolkit
        Platform.startup(() -> {
            try {
                System.out.println("[JavaFXSmokeTest] JavaFX Platform successfully started.");

                // 1. Test LoginView instantiation
                System.out.println("[JavaFXSmokeTest] Instantiating LoginView...");
                LoginView loginView = new LoginView();
                if (loginView.getView() == null) throw new RuntimeException("LoginView returned null root!");
                System.out.println("  -> LoginView initialized OK.");

                // 2. Test UserDashboardView instantiation
                System.out.println("[JavaFXSmokeTest] Instantiating UserDashboardView with demo user...");
                AuthenticationService auth = new AuthenticationService();
                auth.login("driver_user", "User@123");
                UserDashboardView userView = new UserDashboardView();
                if (userView.getView() == null) throw new RuntimeException("UserDashboardView returned null root!");
                System.out.println("  -> UserDashboardView initialized OK.");

                // 3. Test AdminDashboardView instantiation
                System.out.println("[JavaFXSmokeTest] Instantiating AdminDashboardView with demo admin...");
                auth.login("admin", "Admin@123");
                AdminDashboardView adminView = new AdminDashboardView();
                if (adminView.getView() == null) throw new RuntimeException("AdminDashboardView returned null root!");
                System.out.println("  -> AdminDashboardView initialized OK.");

                success[0] = true;
            } catch (Exception e) {
                System.err.println("[JavaFXSmokeTest] Error during JavaFX component initialization:");
                e.printStackTrace();
            } finally {
                latch.countDown();
                Platform.exit();
            }
        });

        boolean finishedInTime = latch.await(15, TimeUnit.SECONDS);
        if (!finishedInTime || !success[0]) {
            System.err.println("JavaFX Smoke Test FAILED or timed out!");
            System.exit(1);
        } else {
            System.out.println("==================================================");
            System.out.println("  ALL JAVAFX SCREENS & CONTROLS INITIALIZED 100%! ");
            System.out.println("==================================================");
            System.exit(0);
        }
    }
}
