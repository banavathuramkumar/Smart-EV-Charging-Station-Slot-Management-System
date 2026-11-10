package com.evchargehub.ui;

import com.evchargehub.exceptions.InvalidUserException;
import com.evchargehub.model.Person;
import com.evchargehub.model.User;
import com.evchargehub.service.AuthenticationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.sql.SQLException;

/**
 * Modern Authentication Screen offering Login and User Registration.
 * Demonstrates:
 * - JavaFX Application Controls (TextField, PasswordField, RadioButton, ToggleGroup)
 * - Layout Panes (BorderPane, VBox, HBox, StackPane)
 * - Effects and styling
 */
public class LoginView {

    private final AuthenticationService authService;
    private final BorderPane root;

    public LoginView() {
        this.authService = new AuthenticationService();
        this.root = new BorderPane();
        buildUI();
    }

    private void buildUI() {
        root.setStyle("-fx-background-color: #0b1120;");

        // --- Left Hero Panel ---
        VBox heroPane = new VBox(20);
        heroPane.setPrefWidth(480);
        heroPane.setPadding(new Insets(50, 40, 50, 50));
        heroPane.setAlignment(Pos.CENTER_LEFT);
        heroPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #131d31); -fx-border-color: #1e293b; -fx-border-width: 0 1px 0 0;");

        // Brand Pill
        HBox brandPill = new HBox(8);
        brandPill.setAlignment(Pos.CENTER_LEFT);
        Circle statusDot = new Circle(5, Color.web("#10b981"));
        Label orgLabel = new Label("EV CHARGEHUB • ENTERPRISE NETWORK");
        orgLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #10b981; -fx-letter-spacing: 1px;");
        brandPill.getChildren().addAll(statusDot, orgLabel);

        Label titleLabel = new Label("EV ChargeHub");
        titleLabel.setStyle("-fx-font-size: 38px; -fx-font-weight: 800; -fx-text-fill: #ffffff;");
        titleLabel.setEffect(UITheme.createGreenGlow());

        Label subtitleLabel = new Label("Smart EV Charging Station & Slot Management System");
        subtitleLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #94a3b8;");
        subtitleLabel.setWrapText(true);

        VBox featureList = new VBox(12);
        featureList.setPadding(new Insets(15, 0, 15, 0));
        featureList.getChildren().addAll(
            createFeatureItem("⚡ Smart Slot Booking with Concurrency Protection"),
            createFeatureItem("🔋 Background Multithreaded Charging Simulation"),
            createFeatureItem("💳 Automated Billing & Wallet Transaction Management"),
            createFeatureItem("⏳ Active Waiting Queue with Inter-thread Notification"),
            createFeatureItem("📊 Real-time SQLite Telemetry & Java I/O Reports")
        );

        heroPane.getChildren().addAll(brandPill, titleLabel, subtitleLabel, featureList);

        // --- Right Auth Form Panel ---
        StackPane formContainer = new StackPane();
        formContainer.setPadding(new Insets(40));
        formContainer.setAlignment(Pos.CENTER);

        VBox authCard = new VBox(20);
        authCard.setMaxWidth(460);
        authCard.setPadding(new Insets(35, 30, 35, 30));
        authCard.getStyleClass().add("card");
        authCard.setEffect(UITheme.createCardShadow());

        // Toggle Buttons for Login vs Register
        HBox tabHeader = new HBox(10);
        tabHeader.setAlignment(Pos.CENTER);
        ToggleGroup authModeGroup = new ToggleGroup();
        RadioButton rbLogin = new RadioButton("Sign In");
        rbLogin.setToggleGroup(authModeGroup);
        rbLogin.setSelected(true);
        rbLogin.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");

        RadioButton rbRegister = new RadioButton("Register New Account");
        rbRegister.setToggleGroup(authModeGroup);
        rbRegister.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;");
        tabHeader.getChildren().addAll(rbLogin, rbRegister);

        // Form Fields Area
        VBox dynamicForm = new VBox(14);

        // Build Login Form
        VBox loginFormBox = buildLoginForm();
        // Build Register Form
        VBox registerFormBox = buildRegisterForm();

        dynamicForm.getChildren().add(loginFormBox);

        authModeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            dynamicForm.getChildren().clear();
            if (newVal == rbLogin) {
                dynamicForm.getChildren().add(loginFormBox);
            } else {
                dynamicForm.getChildren().add(registerFormBox);
            }
        });

        authCard.getChildren().addAll(tabHeader, new Separator(), dynamicForm);
        formContainer.getChildren().add(authCard);

        root.setLeft(heroPane);
        root.setCenter(formContainer);
    }

    private VBox buildLoginForm() {
        VBox box = new VBox(14);

        Label lblHeader = new Label("Welcome Back");
        lblHeader.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        Label lblSub = new Label("Sign in with your username and password to proceed.");
        lblSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        VBox uBox = new VBox(5);
        Label lblUser = new Label("Username");
        lblUser.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
        TextField txtUsername = new TextField();
        txtUsername.setPromptText("Enter username");
        uBox.getChildren().addAll(lblUser, txtUsername);

        VBox pBox = new VBox(5);
        Label lblPass = new Label("Password");
        lblPass.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Enter password");
        pBox.getChildren().addAll(lblPass, txtPassword);

        Button btnLogin = new Button("Sign In to Dashboard");
        btnLogin.getStyleClass().add("btn-primary");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(e -> handleLogin(txtUsername.getText(), txtPassword.getText()));

        box.getChildren().addAll(lblHeader, lblSub, uBox, pBox, btnLogin);
        return box;
    }

    private VBox buildRegisterForm() {
        VBox box = new VBox(12);

        Label lblHeader = new Label("Create EV Driver Account");
        lblHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        TextField txtRegUser = new TextField();
        txtRegUser.setPromptText("Desired Username (min 3 chars)");

        PasswordField txtRegPass = new PasswordField();
        txtRegPass.setPromptText("Password (min 6 chars)");

        TextField txtFullName = new TextField();
        txtFullName.setPromptText("Full Legal Name");

        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email Address (e.g. driver@evchargehub.com)");

        TextField txtPhone = new TextField();
        txtPhone.setPromptText("10-Digit Mobile Phone Number");

        TextField txtLicense = new TextField();
        txtLicense.setPromptText("Driving License Number (optional)");

        Button btnRegister = new Button("Create Account & Claim $100 Wallet Credit");
        btnRegister.getStyleClass().add("btn-primary");
        btnRegister.setMaxWidth(Double.MAX_VALUE);
        btnRegister.setOnAction(e -> {
            try {
                User registered = authService.registerUser(
                    txtRegUser.getText(),
                    txtRegPass.getText(),
                    txtFullName.getText(),
                    txtEmail.getText(),
                    txtPhone.getText(),
                    txtLicense.getText()
                );
                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Registration Successful",
                    "Welcome, " + registered.getFullName() + "!\nYour account has been created with a complimentary $100.00 wallet credit.\nPlease sign in to begin.");
            } catch (InvalidUserException ex) {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Registration Validation", ex.getMessage());
            } catch (SQLException ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to register user: " + ex.getMessage());
            }
        });

        box.getChildren().addAll(lblHeader, txtRegUser, txtRegPass, txtFullName, txtEmail, txtPhone, txtLicense, btnRegister);
        return box;
    }

    private void handleLogin(String username, String password) {
        try {
            Person authenticatedPerson = authService.login(username, password);
            ViewNavigator.handlePostLogin(authenticatedPerson);
        } catch (InvalidUserException e) {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Login Failed", e.getMessage());
        } catch (SQLException e) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database: " + e.getMessage());
        }
    }

    private HBox createFeatureItem(String text) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");
        box.getChildren().add(lbl);
        return box;
    }

    public Parent getView() {
        return root;
    }
}
