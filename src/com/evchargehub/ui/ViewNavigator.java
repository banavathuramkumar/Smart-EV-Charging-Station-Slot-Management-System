package com.evchargehub.ui;

import com.evchargehub.model.Admin;
import com.evchargehub.model.Person;
import com.evchargehub.service.AuthenticationService;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

/**
 * Screen router and scene manager for the JavaFX desktop application.
 */
public class ViewNavigator {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("EV ChargeHub - Smart EV Charging Station & Slot Management System");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(720);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void showLogin() {
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getView(), 1150, 750);
        applyStylesheet(scene);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void showUserDashboard() {
        UserDashboardView userView = new UserDashboardView();
        Scene scene = new Scene(userView.getView(), 1200, 800);
        applyStylesheet(scene);
        primaryStage.setScene(scene);
    }

    public static void showAdminDashboard() {
        AdminDashboardView adminView = new AdminDashboardView();
        Scene scene = new Scene(adminView.getView(), 1200, 800);
        applyStylesheet(scene);
        primaryStage.setScene(scene);
    }

    public static void handlePostLogin(Person person) {
        if (person instanceof Admin) {
            showAdminDashboard();
        } else {
            showUserDashboard();
        }
    }

    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        try {
            applyStylesheet(alert.getDialogPane().getScene());
        } catch (Exception ignored) {}
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        try {
            applyStylesheet(alert.getDialogPane().getScene());
        } catch (Exception ignored) {}
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void applyStylesheet(Scene scene) {
        if (scene == null) return;
        File cssFile = new File("resources/styles/style.css");
        if (cssFile.exists()) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cssFile.toURI().toString());
        }
    }
}
