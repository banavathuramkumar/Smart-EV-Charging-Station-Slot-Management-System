package com.evchargehub;

import com.evchargehub.database.DatabaseInitializer;
import com.evchargehub.ui.ViewNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main Application Entry Point for EV ChargeHub.
 * - JavaFX Application Skeleton
 * - Application lifecycle (init, start, stop)
 */
public class Main extends Application {

    @Override
    public void init() throws Exception {
        super.init();
        System.out.println("===============================================================");
        System.out.println("  EV ChargeHub - Smart EV Charging Station & Slot Management   ");
        System.out.println("  Enterprise Highway EV Fast Charging & Slot Reservation System ");
        System.out.println("===============================================================");
        // Initialize SQLite database schema and seed initial data
        DatabaseInitializer.initializeDatabase();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ViewNavigator.setPrimaryStage(primaryStage);
        ViewNavigator.showLogin();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("[Main] Application shutting down gracefully.");
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
