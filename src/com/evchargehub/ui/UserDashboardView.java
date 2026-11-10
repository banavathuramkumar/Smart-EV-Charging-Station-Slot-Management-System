package com.evchargehub.ui;

import com.evchargehub.collections.KeyValuePair;
import com.evchargehub.dao.VehicleDAO;
import com.evchargehub.exceptions.BookingException;
import com.evchargehub.exceptions.EVChargeHubException;
import com.evchargehub.model.*;
import com.evchargehub.service.*;
import com.evchargehub.threads.ChargingSessionThread;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main Dashboard for EV Drivers (Users).
 * Demonstrates:
 * - JavaFX Application Controls: ComboBox, ListView, TreeView, RadioButton, TextField, DatePicker, ProgressBar
 * - Menus and Toolbars: MenuBar, Menu, MenuItem, ToolBar, Separators
 * - JavaFX Concurrency: Platform.runLater for thread-safe UI updates
 */
public class UserDashboardView {

    private final BorderPane root;
    private final StackPane contentArea;
    private final StationService stationService;
    private final BookingService bookingService;
    private final ChargingService chargingService;
    private final BillingService billingService;
    private final WaitingQueueService queueService;
    private final VehicleDAO vehicleDAO;

    private User currentUser;
    private Label lblWalletTop;
    private ChargingSession activeSession;

    // Dynamic Live Charging UI components
    private boolean isChargingScreenActive = false;
    private Label lblSimHeader;
    private Label lblBatteryPct;
    private ProgressBar batteryBar;
    private Label lblLiveEnergy;
    private Label lblLiveCost;
    private Label lblLiveElapsed;
    private Button btnStopCharging;

    public UserDashboardView() {
        this.root = new BorderPane();
        this.contentArea = new StackPane();
        this.stationService = new StationService();
        this.bookingService = new BookingService();
        this.chargingService = new ChargingService();
        this.billingService = new BillingService();
        this.queueService = new WaitingQueueService();
        this.vehicleDAO = new VehicleDAO();

        Person p = AuthenticationService.getCurrentUser();
        if (p instanceof User u) {
            this.currentUser = u;
        } else {
            this.currentUser = new User("guest", "", "Guest User", "", "");
        }

        buildUI();
    }

    private void buildUI() {
        root.setStyle("-fx-background-color: #0b1120;");

        // 1. Top Menus & Toolbar
        VBox topHeader = buildTopHeader();
        root.setTop(topHeader);

        // 2. Left Navigation Sidebar
        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        // 3. Central Content Area
        contentArea.setPadding(new Insets(24));
        root.setCenter(contentArea);

        // Default to Overview screen
        showOverviewScreen();
    }

    private VBox buildTopHeader() {
        VBox header = new VBox();

        // --- MenuBar ---
        MenuBar menuBar = new MenuBar();

        Menu menuFile = new Menu("System");
        MenuItem itemRefresh = new MenuItem("🔄 Refresh Data");
        itemRefresh.setOnAction(e -> showOverviewScreen());
        MenuItem itemLogout = new MenuItem("🚪 Sign Out");
        itemLogout.setOnAction(e -> {
            AuthenticationService.logout();
            ViewNavigator.showLogin();
        });
        MenuItem itemExit = new MenuItem("❌ Exit Application");
        itemExit.setOnAction(e -> Platform.exit());
        menuFile.getItems().addAll(itemRefresh, new SeparatorMenuItem(), itemLogout, itemExit);

        Menu menuNavigate = new Menu("Modules");
        MenuItem navOverview = new MenuItem("📊 Overview");
        navOverview.setOnAction(e -> showOverviewScreen());
        MenuItem navStations = new MenuItem("⚡ Charging Stations & Slots");
        navStations.setOnAction(e -> showStationsScreen());
        MenuItem navBook = new MenuItem("📅 Book a Slot");
        navBook.setOnAction(e -> showBookingScreen());
        MenuItem navCharging = new MenuItem("🔋 Live Charging Simulation");
        navCharging.setOnAction(e -> showChargingScreen());
        MenuItem navVehicles = new MenuItem("🚗 My Vehicles");
        navVehicles.setOnAction(e -> showVehiclesScreen());
        MenuItem navHistory = new MenuItem("📋 Booking History");
        navHistory.setOnAction(e -> showHistoryScreen());
        MenuItem navQueue = new MenuItem("⏳ Station Waiting Queue");
        navQueue.setOnAction(e -> showQueueScreen());
        MenuItem navBilling = new MenuItem("💳 Billing & Wallet");
        navBilling.setOnAction(e -> showBillingScreen());
        menuNavigate.getItems().addAll(navOverview, navStations, navBook, navCharging, navVehicles, navHistory, navQueue, navBilling);

        Menu menuHelp = new Menu("Help");
        MenuItem itemAbout = new MenuItem("ℹ️ About EV ChargeHub");
        itemAbout.setOnAction(e -> ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "EV ChargeHub",
            "EV ChargeHub – Smart EV Charging Station & Slot Management System\n" +
            "Enterprise Highway EV Fast Charging & Slot Reservation Network\n" +
            "Vijayawada to Hyderabad Highway Corridor (NH 65)\n" +
            "Technology: Core Java, JavaFX, SQLite JDBC, Multithreading, Generics."));
        menuHelp.getItems().add(itemAbout);

        menuBar.getMenus().addAll(menuFile, menuNavigate, menuHelp);

        // --- ToolBar ---
        ToolBar toolBar = new ToolBar();
        toolBar.setStyle("-fx-background-color: #131d31; -fx-padding: 6 16 6 16;");

        Button btnQuickBook = new Button("⚡ Quick Book");
        btnQuickBook.getStyleClass().add("btn-primary");
        btnQuickBook.setStyle("-fx-font-size: 11px; -fx-padding: 5 12 5 12;");
        btnQuickBook.setOnAction(e -> showBookingScreen());

        Button btnQuickCharge = new Button("🔋 Active Charging");
        btnQuickCharge.getStyleClass().add("btn-secondary");
        btnQuickCharge.setStyle("-fx-font-size: 11px; -fx-padding: 5 12 5 12;");
        btnQuickCharge.setOnAction(e -> showChargingScreen());

        Button btnQuickRefresh = new Button("🔄 Sync DB");
        btnQuickRefresh.getStyleClass().add("btn-secondary");
        btnQuickRefresh.setStyle("-fx-font-size: 11px; -fx-padding: 5 12 5 12;");
        btnQuickRefresh.setOnAction(e -> {
            refreshUserData();
            showOverviewScreen();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lblWalletTop = new Label("Wallet: $" + String.format("%.2f", currentUser.getWalletBalance()));
        lblWalletTop.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 4 10 4 10; -fx-background-color: rgba(16, 185, 129, 0.15); -fx-background-radius: 6;");

        Label userPill = new Label("👤 " + currentUser.getFullName());
        userPill.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px; -fx-font-weight: 600; -fx-padding: 4 10 4 10;");

        toolBar.getItems().addAll(btnQuickBook, btnQuickCharge, btnQuickRefresh, new Separator(), spacer, lblWalletTop, userPill);

        header.getChildren().addAll(menuBar, toolBar);
        return header;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(20, 12, 20, 12));
        sidebar.getStyleClass().add("sidebar");

        Label brand = new Label("⚡ EV ChargeHub");
        brand.getStyleClass().add("sidebar-title");
        Label subtitle = new Label("DRIVER DASHBOARD");
        subtitle.getStyleClass().add("sidebar-subtitle");
        VBox headerBox = new VBox(2, brand, subtitle);
        headerBox.setPadding(new Insets(0, 0, 16, 8));

        Button btnHome = createNavBtn("📊  Overview", e -> showOverviewScreen());
        Button btnStations = createNavBtn("⚡  Browse Stations", e -> showStationsScreen());
        Button btnBook = createNavBtn("📅  Reserve Slot", e -> showBookingScreen());
        Button btnSim = createNavBtn("🔋  Charging Session", e -> showChargingScreen());
        Button btnVehicles = createNavBtn("🚗  My Vehicles", e -> showVehiclesScreen());
        Button btnHistory = createNavBtn("📋  Booking History", e -> showHistoryScreen());
        Button btnQueue = createNavBtn("⏳  Waiting Queue", e -> showQueueScreen());
        Button btnBilling = createNavBtn("💳  Billing & Wallet", e -> showBillingScreen());

        Region vSpacer = new Region();
        VBox.setVgrow(vSpacer, Priority.ALWAYS);

        Button btnLogout = new Button("🚪  Sign Out");
        btnLogout.getStyleClass().add("btn-danger");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setOnAction(e -> {
            AuthenticationService.logout();
            ViewNavigator.showLogin();
        });

        sidebar.getChildren().addAll(headerBox, new Separator(), btnHome, btnStations, btnBook, btnSim, btnVehicles, btnHistory, btnQueue, btnBilling, vSpacer, btnLogout);
        return sidebar;
    }

    private Button createNavBtn(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(handler);
        return btn;
    }

    private void refreshUserData() {
        try {
            var updated = new com.evchargehub.dao.UserDAO().findById(currentUser.getId());
            if (updated instanceof User u) {
                this.currentUser = u;
                lblWalletTop.setText("Wallet: $" + String.format("%.2f", u.getWalletBalance()));
            }
        } catch (SQLException ignored) {}
    }

    // ==========================================
    // SCREEN 1: Overview
    // ==========================================
    private void showOverviewScreen() {
        isChargingScreenActive = false;
        refreshUserData();
        VBox box = new VBox(20);

        HBox titleBox = UITheme.createSectionHeader("Welcome back, " + currentUser.getFullName() + "!",
                "Real-time summary of your EV charging activities, reservations, and station network.");

        // KPI Stat Cards
        HBox statsBox = new HBox(16);
        int myVehiclesCount = 0;
        int activeBookingsCount = 0;
        int totalStationsCount = 0;
        try {
            myVehiclesCount = vehicleDAO.findByOwnerId(currentUser.getId()).size();
            var myBookings = bookingService.getUserBookings(currentUser.getId());
            for (var b : myBookings) {
                if (b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.IN_PROGRESS) {
                    activeBookingsCount++;
                }
            }
            totalStationsCount = stationService.getActiveStations().size();
        } catch (SQLException ignored) {}

        statsBox.getChildren().addAll(
            UITheme.createStatCard("WALLET BALANCE", "$" + String.format("%.2f", currentUser.getWalletBalance()), "Available Credits", "#10b981"),
            UITheme.createStatCard("ACTIVE RESERVATIONS", String.valueOf(activeBookingsCount), "Ready to Charge", "#38bdf8"),
            UITheme.createStatCard("MY REGISTERED EVS", String.valueOf(myVehiclesCount), "Vehicles Linked", "#fbbf24"),
            UITheme.createStatCard("ACTIVE STATIONS", String.valueOf(totalStationsCount), "Online & Ready", "#a78bfa")
        );

        // Active Booking or Charging Alert Banner
        VBox bannerBox = new VBox(10);
        bannerBox.getStyleClass().add("card");
        bannerBox.setStyle("-fx-background-color: #1e293b; -fx-border-color: #38bdf8; -fx-border-radius: 8;");
        Label lblBannerTitle = new Label("⚡ Active Charging & Reservation Status");
        lblBannerTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #38bdf8;");

        if (activeSession != null) {
            Label lblAct = new Label("⚡ Active Charging Session #" + activeSession.getSessionId() + " in Progress!");
            lblAct.setStyle("-fx-text-fill: #34d399; -fx-font-weight: bold; -fx-font-size: 14px;");
            Label lblKwh = new Label("Energy Delivered: " + String.format("%.2f", activeSession.getEnergyDeliveredKWh()) + " kWh | Cost: $" + String.format("%.2f", activeSession.getTotalCost()));
            lblKwh.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");
            Button btnGoMonitor = new Button("🔋 Open Live Charging Monitor");
            btnGoMonitor.getStyleClass().add("btn-primary");
            btnGoMonitor.setOnAction(e -> showChargingScreen());
            bannerBox.getChildren().addAll(lblBannerTitle, lblAct, lblKwh, btnGoMonitor);
        } else {
            Booking nextBooking = null;
            try {
                var bList = bookingService.getUserBookings(currentUser.getId());
                for (var b : bList) {
                    if (b.getStatus() == BookingStatus.CONFIRMED) {
                        nextBooking = b;
                        break;
                    }
                }
            } catch (SQLException ignored) {}

            if (nextBooking != null) {
                final Booking currentNext = nextBooking;
                Label lblInfo = new Label("Station: " + nextBooking.getStationName() + " | Slot: " + nextBooking.getSlotNumber() +
                        " | Date: " + nextBooking.getBookingDate() + " (" + nextBooking.getStartTime() + " - " + nextBooking.getEndTime() + ")");
                lblInfo.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");

                HBox actions = new HBox(12);
                Button btnStartNow = new Button("🚀 Start Charging Session Now");
                btnStartNow.getStyleClass().add("btn-primary");
                btnStartNow.setOnAction(e -> startChargingFromBooking(currentNext));

                Button btnCancel = new Button("Cancel Booking");
                btnCancel.getStyleClass().add("btn-danger");
                btnCancel.setOnAction(e -> {
                    if (ViewNavigator.showConfirmation("Cancel Booking", "Are you sure you want to cancel Booking #" + currentNext.getBookingId() + "?")) {
                        try {
                            bookingService.cancelBooking(currentNext.getBookingId());
                            showOverviewScreen();
                        } catch (SQLException ex) {
                            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
                        }
                    }
                });
                actions.getChildren().addAll(btnStartNow, btnCancel);
                bannerBox.getChildren().addAll(lblBannerTitle, lblInfo, actions);
            } else {
                Label lblNone = new Label("No active reservations at the moment. Browse available slots and book your charging time!");
                lblNone.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
                Button btnBookPrompt = new Button("📅 Book a Charging Slot Now");
                btnBookPrompt.getStyleClass().add("btn-primary");
                btnBookPrompt.setOnAction(e -> showBookingScreen());
                bannerBox.getChildren().addAll(lblBannerTitle, lblNone, btnBookPrompt);
            }
        }

        // Quick Actions Row
        HBox quickActions = new HBox(14);
        Button act1 = new Button("⚡ Browse Available Slots");
        act1.getStyleClass().add("btn-secondary");
        act1.setOnAction(e -> showStationsScreen());

        Button act2 = new Button("🚗 Register a New EV");
        act2.getStyleClass().add("btn-secondary");
        act2.setOnAction(e -> showVehiclesScreen());

        Button act3 = new Button("⏳ Join Station Waiting Queue");
        act3.getStyleClass().add("btn-secondary");
        act3.setOnAction(e -> showQueueScreen());

        quickActions.getChildren().addAll(act1, act2, act3);

        box.getChildren().addAll(titleBox, statsBox, bannerBox, quickActions);
        setContent(box);
    }

    // ==========================================
    // SCREEN 2: Browse Stations & Slots (TreeView & ComboBox)
    // ==========================================
    private void showStationsScreen() {
        isChargingScreenActive = false;
        VBox box = new VBox(16);
        box.getChildren().add(UITheme.createSectionHeader("Charging Station Network",
                "Explore real-time station availability, slot power ratings, and connector types."));

        // Station Filter & TreeView Controls
        HBox filterBar = new HBox(14);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Label lblFilter = new Label("Filter Station:");
        lblFilter.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: 600;");

        ComboBox<String> stationCombo = new ComboBox<>();
        stationCombo.getItems().add("All Stations");

        // TreeView showing hierarchy: Station -> Connector Type -> Slots
        TreeItem<String> rootTreeItem = new TreeItem<>("EV ChargeHub Network - Vijayawada to Hyderabad Corridor (NH 65)");
        rootTreeItem.setExpanded(true);
        TreeView<String> networkTreeView = new TreeView<>(rootTreeItem);
        networkTreeView.setPrefHeight(450);

        try {
            var stations = stationService.getAllStations();
            for (var s : stations) {
                stationCombo.getItems().add(s.getName());
                TreeItem<String> stationItem = new TreeItem<>(s.getName() + " (" + s.getLocation() + ") - Speed: " + s.getChargingSpeedKW() + "kW [$" + s.getPricePerKWh() + "/kWh]");
                stationItem.setExpanded(true);

                var slots = stationService.getSlotsByStation(s.getStationId());
                for (var slot : slots) {
                    TreeItem<String> slotItem = new TreeItem<>("Slot " + slot.getSlotNumber() + " | " + slot.getConnectorType() + " | " + slot.getChargingPowerKW() + " kW | Status: " + slot.getStatus());
                    stationItem.getChildren().add(slotItem);
                }
                rootTreeItem.getChildren().add(stationItem);
            }
        } catch (SQLException e) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "DB Error", e.getMessage());
        }

        stationCombo.setValue("All Stations");
        stationCombo.setOnAction(e -> {
            String selected = stationCombo.getValue();
            if ("All Stations".equals(selected)) {
                for (TreeItem<String> child : rootTreeItem.getChildren()) {
                    child.setExpanded(true);
                }
            } else {
                for (TreeItem<String> child : rootTreeItem.getChildren()) {
                    child.setExpanded(child.getValue().startsWith(selected));
                }
            }
        });

        filterBar.getChildren().addAll(lblFilter, stationCombo);

        Button btnProceedBook = new Button("📅 Book Slot at Selected Station");
        btnProceedBook.getStyleClass().add("btn-primary");
        btnProceedBook.setOnAction(e -> showBookingScreen());

        box.getChildren().addAll(filterBar, networkTreeView, btnProceedBook);
        setContent(box);
    }

    // ==========================================
    // SCREEN 3: Smart Slot Booking
    // ==========================================
    private void showBookingScreen() {
        isChargingScreenActive = false;
        VBox box = new VBox(16);
        box.getChildren().add(UITheme.createSectionHeader("Smart Slot Reservation",
                "Reserve your charging slot with automated conflict detection and concurrency protection."));

        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("card");
        formCard.setMaxWidth(650);

        // 1. Station Dropdown
        VBox stnBox = new VBox(5);
        Label lblStn = new Label("1. Select Charging Station:");
        lblStn.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: 600;");
        ComboBox<ChargingStation> cmbStation = new ComboBox<>();
        cmbStation.setMaxWidth(Double.MAX_VALUE);

        // 2. Slot Dropdown
        VBox slotBox = new VBox(5);
        Label lblSlot = new Label("2. Select Available Slot:");
        lblSlot.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: 600;");
        ComboBox<ChargingSlot> cmbSlot = new ComboBox<>();
        cmbSlot.setMaxWidth(Double.MAX_VALUE);

        // Populate stations and hook listener
        try {
            var activeStations = stationService.getActiveStations();
            cmbStation.setItems(FXCollections.observableArrayList(activeStations));
            if (!activeStations.isEmpty()) {
                cmbStation.setValue(activeStations.get(0));
                var slots = stationService.getAvailableSlots(activeStations.get(0).getStationId());
                cmbSlot.setItems(FXCollections.observableArrayList(slots));
                if (!slots.isEmpty()) cmbSlot.setValue(slots.get(0));
            }
        } catch (SQLException ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "DB Error", ex.getMessage());
        }

        cmbStation.setOnAction(e -> {
            ChargingStation s = cmbStation.getValue();
            if (s != null) {
                try {
                    var slots = stationService.getAvailableSlots(s.getStationId());
                    cmbSlot.setItems(FXCollections.observableArrayList(slots));
                    if (!slots.isEmpty()) cmbSlot.setValue(slots.get(0));
                    else cmbSlot.setValue(null);
                } catch (SQLException ex) {
                    cmbSlot.getItems().clear();
                }
            }
        });

        stnBox.getChildren().addAll(lblStn, cmbStation);
        slotBox.getChildren().addAll(lblSlot, cmbSlot);

        // 3. Vehicle Dropdown
        VBox vehBox = new VBox(5);
        Label lblVeh = new Label("3. Select Your Vehicle:");
        lblVeh.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: 600;");
        ComboBox<Vehicle> cmbVehicle = new ComboBox<>();
        cmbVehicle.setMaxWidth(Double.MAX_VALUE);
        try {
            var userVehicles = vehicleDAO.findByOwnerId(currentUser.getId());
            cmbVehicle.setItems(FXCollections.observableArrayList(userVehicles));
            if (!userVehicles.isEmpty()) cmbVehicle.setValue(userVehicles.get(0));
        } catch (SQLException ignored) {}
        vehBox.getChildren().addAll(lblVeh, cmbVehicle);

        // 4. Date and Time
        HBox dateRow = new HBox(16);
        VBox dBox = new VBox(5);
        Label lblDate = new Label("4. Booking Date:");
        lblDate.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: 600;");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        dBox.getChildren().addAll(lblDate, datePicker);
        HBox.setHgrow(dBox, Priority.ALWAYS);

        VBox tBox = new VBox(5);
        Label lblTime = new Label("5. Start Time (HH:mm):");
        lblTime.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: 600;");
        TextField txtStartTime = new TextField(LocalTime.now().plusMinutes(5).format(DateTimeFormatter.ofPattern("HH:mm")));
        txtStartTime.setPromptText("e.g. 14:30");
        tBox.getChildren().addAll(lblTime, txtStartTime);
        HBox.setHgrow(tBox, Priority.ALWAYS);

        dateRow.getChildren().addAll(dBox, tBox);

        // 5. Duration Dropdown
        VBox durBox = new VBox(5);
        Label lblDur = new Label("6. Charging Duration:");
        lblDur.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: 600;");
        ComboBox<KeyValuePair<Double, String>> cmbDuration = new ComboBox<>();
        cmbDuration.getItems().addAll(
            new KeyValuePair<>(0.5, "30 Minutes (Quick Top-up)"),
            new KeyValuePair<>(1.0, "1 Hour (Standard Charge)"),
            new KeyValuePair<>(1.5, "1.5 Hours"),
            new KeyValuePair<>(2.0, "2 Hours (Full Charge)"),
            new KeyValuePair<>(3.0, "3 Hours")
        );
        cmbDuration.setValue(cmbDuration.getItems().get(1));
        durBox.getChildren().addAll(lblDur, cmbDuration);

        // Submit Button
        Button btnConfirmBooking = new Button("🔒 Confirm & Reserve Slot");
        btnConfirmBooking.getStyleClass().add("btn-primary");
        btnConfirmBooking.setMaxWidth(Double.MAX_VALUE);
        btnConfirmBooking.setOnAction(e -> {
            ChargingStation selectedStn = cmbStation.getValue();
            ChargingSlot selectedSlot = cmbSlot.getValue();
            Vehicle selectedVeh = cmbVehicle.getValue();

            if (selectedStn == null) {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Input Error", "Please select a charging station.");
                return;
            }
            if (selectedSlot == null) {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Slot Unavailable", "No available slot selected. Please check another station or join the waiting queue.");
                return;
            }
            if (selectedVeh == null) {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "No Vehicle", "Please add and select an EV vehicle before booking.");
                return;
            }
            if (datePicker.getValue() == null) {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Input Error", "Please choose a booking date.");
                return;
            }

            try {
                double duration = cmbDuration.getValue().getKey();
                String dateStr = datePicker.getValue().toString();
                String timeStr = txtStartTime.getText().trim();

                Booking booking = bookingService.bookSlot(
                    currentUser.getId(),
                    selectedStn.getStationId(),
                    selectedSlot.getSlotId(),
                    selectedVeh.getVehicleId(),
                    dateStr,
                    timeStr,
                    duration
                );

                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Booking Confirmed",
                    "Success! Booking #" + booking.getBookingId() + " confirmed for " + booking.getBookingDate() + " from " + booking.getStartTime() + " to " + booking.getEndTime() + ".\n" +
                    "Station: " + selectedStn.getName() + " (Slot " + selectedSlot.getSlotNumber() + ")");

                showHistoryScreen();

            } catch (BookingException be) {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Booking Conflict", be.getMessage());
            } catch (SQLException sqlEx) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Database Error", sqlEx.getMessage());
            }
        });

        formCard.getChildren().addAll(stnBox, slotBox, vehBox, dateRow, durBox, btnConfirmBooking);
        box.getChildren().add(formCard);
        setContent(box);
    }

    // ==========================================
    // SCREEN 4: Multithreaded Charging Session Simulation
    // ==========================================
    private void showChargingScreen() {
        isChargingScreenActive = true;
        VBox box = new VBox(20);
        box.getChildren().add(UITheme.createSectionHeader("Simulated Charging Session Monitor",
                "Real-time background multithreaded simulation with dynamic energy metrics and live UI telemetry."));

        VBox simCard = new VBox(20);
        simCard.getStyleClass().add("card");
        simCard.setMaxWidth(750);
        simCard.setAlignment(Pos.CENTER);

        // Header info
        lblSimHeader = new Label(activeSession != null ? "⚡ Charging in Progress (Session #" + activeSession.getSessionId() + ")" : "No Active Charging Session Running");
        lblSimHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #10b981;");

        // Battery Gauge & Progress Bar
        VBox batteryBox = new VBox(8);
        batteryBox.setAlignment(Pos.CENTER);
        lblBatteryPct = new Label("Battery Level: 20.0%");
        lblBatteryPct.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: #34d399;");

        batteryBar = new ProgressBar(0.20);
        batteryBar.setPrefWidth(550);
        batteryBar.setPrefHeight(25);
        batteryBar.setStyle("-fx-accent: #10b981;");
        batteryBox.getChildren().addAll(lblBatteryPct, batteryBar);

        // Dynamic Telemetry Metrics
        HBox metricsGrid = new HBox(16);
        metricsGrid.setAlignment(Pos.CENTER);

        double initEnergy = activeSession != null ? activeSession.getEnergyDeliveredKWh() : 0.0;
        double initCost = activeSession != null ? activeSession.getTotalCost() : 0.0;

        lblLiveEnergy = new Label(String.format("%.2f kWh", initEnergy));
        lblLiveCost = new Label(String.format("$%.2f", initCost));
        lblLiveElapsed = new Label("00:00");

        VBox mEnergy = createLiveStatCard("ENERGY DELIVERED", lblLiveEnergy, "Simulated Output", "#10b981");
        VBox mCost = createLiveStatCard("ESTIMATED COST", lblLiveCost, "Simulated Calculation", "#38bdf8");
        VBox mElapsed = createLiveStatCard("ELAPSED TIME", lblLiveElapsed, "Simulation Clock", "#fbbf24");
        metricsGrid.getChildren().addAll(mEnergy, mCost, mElapsed);

        Label lblNotice = new Label("💡 Academic Simulation Notice: Energy (kWh) = Power (kW) × Time (hrs); Cost = Energy × Tariff. 1 real second = 1 simulation minute.");
        lblNotice.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-style: italic;");

        // Action Buttons
        HBox buttonBar = new HBox(14);
        buttonBar.setAlignment(Pos.CENTER);

        btnStopCharging = new Button("🛑 Stop Charging & Proceed to Payment");
        btnStopCharging.getStyleClass().add("btn-danger");
        btnStopCharging.setDisable(activeSession == null);

        Button btnSelectBooking = new Button("⚡ Select Confirmed Booking to Start");
        btnSelectBooking.getStyleClass().add("btn-primary");
        btnSelectBooking.setOnAction(e -> showHistoryScreen());

        buttonBar.getChildren().addAll(btnSelectBooking, btnStopCharging);

        simCard.getChildren().addAll(lblSimHeader, batteryBox, metricsGrid, lblNotice, buttonBar);

        if (activeSession != null) {
            btnStopCharging.setOnAction(e -> {
                try {
                    chargingService.stopSession(activeSession, 0);
                } catch (SQLException ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error Stopping Session", ex.getMessage());
                }
            });
        }

        box.getChildren().add(simCard);
        setContent(box);
    }

    private VBox createLiveStatCard(String title, Label valLabel, String subtitle, String accentColor) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");
        card.setEffect(UITheme.createCardShadow());
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setPrefWidth(220);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("stat-label");

        valLabel.getStyleClass().add("stat-value");
        if (accentColor != null) {
            valLabel.setStyle("-fx-text-fill: " + accentColor + ";");
        }

        Label lblSub = new Label(subtitle);
        lblSub.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        card.getChildren().addAll(lblTitle, valLabel, lblSub);
        UITheme.applyHoverScale(card);
        return card;
    }

    private void startChargingFromBooking(Booking b) {
        try {
            ChargingStation stn = stationService.getStationById(b.getStationId());
            ChargingSlot slot = new com.evchargehub.dao.SlotDAO().findById(b.getSlotId());
            Vehicle veh = vehicleDAO.findById(b.getVehicleId());

            // Launch multithreaded simulation
            this.activeSession = chargingService.startSession(b, veh, stn, slot, new ChargingSessionThread.SessionUpdateListener() {
                @Override
                public void onProgress(double currentEnergyKWh, double currentCost, int elapsedSeconds, double batteryPercentage) {
                    Platform.runLater(() -> {
                        if (isChargingScreenActive) {
                            if (lblBatteryPct != null) lblBatteryPct.setText(String.format("Battery Level: %.1f%%", batteryPercentage));
                            if (batteryBar != null) batteryBar.setProgress(batteryPercentage / 100.0);
                            if (lblLiveEnergy != null) lblLiveEnergy.setText(String.format("%.2f kWh", currentEnergyKWh));
                            if (lblLiveCost != null) lblLiveCost.setText(String.format("$%.2f", currentCost));
                            if (lblLiveElapsed != null) lblLiveElapsed.setText(String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60));
                        }
                    });
                }

                @Override
                public void onComplete(ChargingSession completedSession) {
                    Platform.runLater(() -> {
                        activeSession = null;
                        isChargingScreenActive = false;
                        ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Charging Complete",
                            "Simulated charging session completed!\nTotal Energy Delivered: " + String.format("%.2f", completedSession.getEnergyDeliveredKWh()) +
                            " kWh\nTotal Cost: $" + String.format("%.2f", completedSession.getTotalCost()));
                        showBillingForSession(completedSession);
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    Platform.runLater(() -> ViewNavigator.showAlert(Alert.AlertType.ERROR, "Session Error", errorMessage));
                }
            });

            showChargingScreen();

        } catch (Exception ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Cannot Start Session", ex.getMessage());
        }
    }

    // ==========================================
    // SCREEN 5: Vehicle Management
    // ==========================================
    private void showVehiclesScreen() {
        isChargingScreenActive = false;
        VBox box = new VBox(16);
        box.getChildren().add(UITheme.createSectionHeader("My Registered Electric Vehicles",
                "Manage your registered EV vehicles, battery specs, and connector standards."));

        TableView<Vehicle> table = new TableView<>();
        table.setPrefHeight(340);

        TableColumn<Vehicle, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        colId.setPrefWidth(60);

        TableColumn<Vehicle, String> colReg = new TableColumn<>("Registration No.");
        colReg.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        colReg.setPrefWidth(140);

        TableColumn<Vehicle, String> colBrand = new TableColumn<>("Brand");
        colBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        colBrand.setPrefWidth(120);

        TableColumn<Vehicle, String> colModel = new TableColumn<>("Model");
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colModel.setPrefWidth(140);

        TableColumn<Vehicle, Double> colCap = new TableColumn<>("Battery (kWh)");
        colCap.setCellValueFactory(new PropertyValueFactory<>("batteryCapacityKWh"));
        colCap.setPrefWidth(110);

        TableColumn<Vehicle, String> colConn = new TableColumn<>("Connector Type");
        colConn.setCellValueFactory(new PropertyValueFactory<>("connectorType"));
        colConn.setPrefWidth(120);

        table.getColumns().addAll(colId, colReg, colBrand, colModel, colCap, colConn);

        try {
            var list = vehicleDAO.findByOwnerId(currentUser.getId());
            table.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "DB Error", ex.getMessage());
        }

        // Add Vehicle Form Card
        VBox addCard = new VBox(12);
        addCard.getStyleClass().add("card");
        Label lblAdd = new Label("➕ Add New Vehicle to Profile");
        lblAdd.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        HBox inputs = new HBox(10);
        TextField txtReg = new TextField();
        txtReg.setPromptText("Reg No (e.g. AP 16 EV 1234)");

        TextField txtBrand = new TextField();
        txtBrand.setPromptText("Brand (e.g. Tata / MG / Hyundai)");

        TextField txtModel = new TextField();
        txtModel.setPromptText("Model (e.g. Nexon EV / Ioniq 5)");

        TextField txtCap = new TextField();
        txtCap.setPromptText("Capacity kWh (e.g. 50.0)");

        ComboBox<String> cmbConn = new ComboBox<>();
        cmbConn.getItems().addAll("CCS2", "Type 2", "CHAdeMO", "GB/T");
        cmbConn.setValue("CCS2");

        inputs.getChildren().addAll(txtReg, txtBrand, txtModel, txtCap, cmbConn);

        HBox btns = new HBox(12);
        Button btnSaveVeh = new Button("Save Vehicle");
        btnSaveVeh.getStyleClass().add("btn-primary");
        btnSaveVeh.setOnAction(e -> {
            try {
                if (txtReg.getText().isEmpty() || txtBrand.getText().isEmpty() || txtModel.getText().isEmpty()) {
                    ViewNavigator.showAlert(Alert.AlertType.WARNING, "Input Error", "Please fill in all vehicle details.");
                    return;
                }
                double cap = Double.parseDouble(txtCap.getText().trim());
                Vehicle v = new Vehicle(currentUser.getId(), txtReg.getText().trim(), txtBrand.getText().trim(), txtModel.getText().trim(), cap, cmbConn.getValue());
                vehicleDAO.insert(v);
                showVehiclesScreen();
            } catch (NumberFormatException nfe) {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Input Error", "Battery capacity must be a valid decimal number.");
            } catch (SQLException ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        });

        Button btnDeleteVeh = new Button("🗑️ Delete Selected");
        btnDeleteVeh.getStyleClass().add("btn-danger");
        btnDeleteVeh.setOnAction(e -> {
            Vehicle selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (ViewNavigator.showConfirmation("Delete Vehicle", "Are you sure you want to delete " + selected.getRegistrationNumber() + "?")) {
                    try {
                        vehicleDAO.delete(selected.getVehicleId());
                        showVehiclesScreen();
                    } catch (SQLException ex) {
                        ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
                    }
                }
            } else {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a vehicle from the table to delete.");
            }
        });

        btns.getChildren().addAll(btnSaveVeh, btnDeleteVeh);
        addCard.getChildren().addAll(lblAdd, inputs, btns);

        box.getChildren().addAll(table, addCard);
        setContent(box);
    }

    // ==========================================
    // SCREEN 6: Booking History
    // ==========================================
    private void showHistoryScreen() {
        isChargingScreenActive = false;
        VBox box = new VBox(16);
        box.getChildren().add(UITheme.createSectionHeader("Booking History & Active Reservations",
                "View past and upcoming slot bookings. You can cancel upcoming bookings or start charging."));

        TableView<Booking> table = new TableView<>();
        table.setPrefHeight(420);

        TableColumn<Booking, Integer> colId = new TableColumn<>("Booking ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colId.setPrefWidth(80);

        TableColumn<Booking, String> colStn = new TableColumn<>("Station Name");
        colStn.setCellValueFactory(new PropertyValueFactory<>("stationName"));
        colStn.setPrefWidth(180);

        TableColumn<Booking, String> colSlot = new TableColumn<>("Slot");
        colSlot.setCellValueFactory(new PropertyValueFactory<>("slotNumber"));
        colSlot.setPrefWidth(90);

        TableColumn<Booking, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        colDate.setPrefWidth(100);

        TableColumn<Booking, String> colStart = new TableColumn<>("Start");
        colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colStart.setPrefWidth(80);

        TableColumn<Booking, String> colEnd = new TableColumn<>("End");
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colEnd.setPrefWidth(80);

        TableColumn<Booking, Double> colDur = new TableColumn<>("Duration (Hrs)");
        colDur.setCellValueFactory(new PropertyValueFactory<>("durationHours"));
        colDur.setPrefWidth(100);

        TableColumn<Booking, BookingStatus> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(110);

        table.getColumns().addAll(colId, colStn, colSlot, colDate, colStart, colEnd, colDur, colStatus);

        try {
            var list = bookingService.getUserBookings(currentUser.getId());
            table.setItems(FXCollections.observableArrayList(list));
        } catch (SQLException ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "DB Error", ex.getMessage());
        }

        HBox actions = new HBox(12);
        Button btnStartSelected = new Button("🚀 Start Charging Session for Selected Booking");
        btnStartSelected.getStyleClass().add("btn-primary");
        btnStartSelected.setOnAction(e -> {
            Booking b = table.getSelectionModel().getSelectedItem();
            if (b != null) {
                if (b.getStatus() == BookingStatus.CONFIRMED) {
                    startChargingFromBooking(b);
                } else {
                    ViewNavigator.showAlert(Alert.AlertType.WARNING, "Invalid State", "Selected booking is already " + b.getStatus() + ".");
                }
            } else {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a confirmed booking from the table.");
            }
        });

        Button btnCancel = new Button("Cancel Booking");
        btnCancel.getStyleClass().add("btn-danger");
        btnCancel.setOnAction(e -> {
            Booking b = table.getSelectionModel().getSelectedItem();
            if (b != null) {
                if (b.getStatus() == BookingStatus.CONFIRMED) {
                    if (ViewNavigator.showConfirmation("Cancel Booking", "Cancel Booking #" + b.getBookingId() + "?")) {
                        try {
                            bookingService.cancelBooking(b.getBookingId());
                            showHistoryScreen();
                        } catch (SQLException ex) {
                            ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
                        }
                    }
                } else {
                    ViewNavigator.showAlert(Alert.AlertType.WARNING, "Cannot Cancel", "Only CONFIRMED bookings can be cancelled.");
                }
            } else {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to cancel.");
            }
        });

        actions.getChildren().addAll(btnStartSelected, btnCancel);
        box.getChildren().addAll(table, actions);
        setContent(box);
    }

    // ==========================================
    // SCREEN 7: Waiting Queue (Queue Management)
    // ==========================================
    private void showQueueScreen() {
        isChargingScreenActive = false;
        VBox box = new VBox(16);
        box.getChildren().add(UITheme.createSectionHeader("Station Waiting Queue",
                "When all slots are full, join the automated waiting queue. First-in, first-out inter-thread notification."));

        HBox queueControls = new HBox(14);
        queueControls.setAlignment(Pos.CENTER_LEFT);

        Label lblStation = new Label("Select Station Queue:");
        lblStation.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: 600;");

        ComboBox<ChargingStation> cmbStation = new ComboBox<>();
        try {
            var stns = stationService.getAllStations();
            cmbStation.setItems(FXCollections.observableArrayList(stns));
            if (!stns.isEmpty()) cmbStation.setValue(stns.get(0));
        } catch (SQLException ignored) {}

        // Station Waiting List View
        ListView<String> queueListView = new ListView<>();
        queueListView.setPrefHeight(350);

        Runnable loadQueue = () -> {
            queueListView.getItems().clear();
            ChargingStation s = cmbStation.getValue();
            if (s != null) {
                try {
                    var entries = queueService.getQueueForStation(s.getStationId());
                    if (entries.isEmpty()) {
                        queueListView.getItems().add("Queue is currently empty for " + s.getName() + ".");
                    } else {
                        for (int i = 0; i < entries.size(); i++) {
                            QueueEntry q = entries.get(i);
                            queueListView.getItems().add("Position #" + (i + 1) + ": " + q.getUserName() + " (Joined: " + q.getJoinTime() + ")");
                        }
                    }
                } catch (SQLException ex) {
                    queueListView.getItems().add("Error loading queue: " + ex.getMessage());
                }
            }
        };

        cmbStation.setOnAction(e -> loadQueue.run());
        loadQueue.run();

        Button btnJoin = new Button("⏳ Join Waiting Queue");
        btnJoin.getStyleClass().add("btn-primary");
        btnJoin.setOnAction(e -> {
            ChargingStation s = cmbStation.getValue();
            if (s != null) {
                try {
                    queueService.joinQueue(currentUser.getId(), s.getStationId(), currentUser.getFullName(), s.getName(), currentUser.getEmail());
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Queue Joined", "You have joined the waiting queue for " + s.getName() + ".");
                    loadQueue.run();
                } catch (Exception ex) {
                    ViewNavigator.showAlert(Alert.AlertType.WARNING, "Queue Notice", ex.getMessage());
                }
            }
        });

        Button btnLeave = new Button("Leave Queue");
        btnLeave.getStyleClass().add("btn-secondary");
        btnLeave.setOnAction(e -> {
            ChargingStation s = cmbStation.getValue();
            if (s != null) {
                try {
                    queueService.leaveQueue(currentUser.getId(), s.getStationId());
                    loadQueue.run();
                } catch (SQLException ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
                }
            }
        });

        queueControls.getChildren().addAll(lblStation, cmbStation, btnJoin, btnLeave);
        box.getChildren().addAll(queueControls, queueListView);
        setContent(box);
    }

    // ==========================================
    // SCREEN 8: Billing, Wallet & Transactions
    // ==========================================
    private void showBillingScreen() {
        showBillingForSession(null);
    }

    private void showBillingForSession(ChargingSession completedSession) {
        isChargingScreenActive = false;
        refreshUserData();
        VBox box = new VBox(16);
        box.getChildren().add(UITheme.createSectionHeader("Billing & Simulated Payments",
                "Instant electronic receipt generation, wallet management, and transaction ledger."));

        // If arriving from completed session, show payment checkout card
        if (completedSession != null) {
            VBox payCard = new VBox(14);
            payCard.getStyleClass().add("card");
            payCard.setStyle("-fx-border-color: #10b981; -fx-border-width: 2;");
            payCard.setMaxWidth(600);

            Label lblCheckout = new Label("🧾 Invoice for Completed Session #" + completedSession.getSessionId());
            lblCheckout.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #10b981;");

            Label lblEnergy = new Label("Energy Delivered: " + String.format("%.2f", completedSession.getEnergyDeliveredKWh()) + " kWh");
            lblEnergy.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");

            Label lblTotal = new Label("Total Payable: $" + String.format("%.2f", completedSession.getTotalCost()));
            lblTotal.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

            // Payment Method Radio Buttons
            Label lblMethod = new Label("Select Payment Mode (Simulated):");
            lblMethod.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: 600;");

            ToggleGroup payGroup = new ToggleGroup();
            RadioButton rbWallet = new RadioButton("💳 EV ChargeHub In-App Wallet (Balance: $" + String.format("%.2f", currentUser.getWalletBalance()) + ")");
            rbWallet.setToggleGroup(payGroup);
            rbWallet.setSelected(true);

            RadioButton rbCard = new RadioButton("🏦 Simulated Credit / Debit Card");
            rbCard.setToggleGroup(payGroup);

            RadioButton rbUPI = new RadioButton("📱 Simulated UPI / Instant Pay");
            rbUPI.setToggleGroup(payGroup);

            VBox payMethods = new VBox(8, rbWallet, rbCard, rbUPI);

            Button btnPayNow = new Button("Confirm Simulated Payment of $" + String.format("%.2f", completedSession.getTotalCost()));
            btnPayNow.getStyleClass().add("btn-primary");
            btnPayNow.setOnAction(e -> {
                String method = rbWallet.isSelected() ? "WALLET" : (rbCard.isSelected() ? "CREDIT_CARD" : "UPI");
                try {
                    Transaction txn = billingService.processPayment(completedSession.getSessionId(), currentUser.getId(), completedSession.getTotalCost(), method);
                    ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Payment Successful",
                        "Payment processed successfully!\nTransaction ID: " + txn.getTransactionId() + "\nAmount: $" + String.format("%.2f", txn.getAmount()) +
                        "\nPayment Method: " + method + "\nStatus: PAID");
                    showBillingScreen();
                } catch (EVChargeHubException | SQLException ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Payment Failed", ex.getMessage());
                }
            });

            payCard.getChildren().addAll(lblCheckout, lblEnergy, lblTotal, lblMethod, payMethods, btnPayNow);
            box.getChildren().add(payCard);
        }

        // Wallet Balance Card & Top-up
        HBox walletRow = new HBox(16);
        VBox wCard = UITheme.createStatCard("CURRENT WALLET BALANCE", "$" + String.format("%.2f", currentUser.getWalletBalance()), "Available Credits", "#10b981");

        VBox topUpCard = new VBox(10);
        topUpCard.getStyleClass().add("card");
        Label lblTopUp = new Label("Simulate Wallet Top-Up");
        lblTopUp.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");
        HBox topUpInputs = new HBox(8);
        TextField txtAmount = new TextField("50.00");
        txtAmount.setPrefWidth(120);
        Button btnAddFunds = new Button("Add Funds");
        btnAddFunds.getStyleClass().add("btn-primary");
        btnAddFunds.setOnAction(e -> {
            try {
                double amt = Double.parseDouble(txtAmount.getText().trim());
                if (amt <= 0) throw new NumberFormatException();
                double newBal = currentUser.getWalletBalance() + amt;
                new com.evchargehub.dao.UserDAO().updateWalletBalance(currentUser.getId(), newBal);
                currentUser.setWalletBalance(newBal);
                refreshUserData();
                showBillingScreen();
            } catch (Exception ex) {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Error", "Enter a valid positive number.");
            }
        });
        topUpInputs.getChildren().addAll(txtAmount, btnAddFunds);
        topUpCard.getChildren().addAll(lblTopUp, topUpInputs);

        walletRow.getChildren().addAll(wCard, topUpCard);

        // Transaction History Table
        Label lblTxnTable = new Label("Transaction History Ledger");
        lblTxnTable.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");

        TableView<Transaction> table = new TableView<>();
        table.setPrefHeight(300);

        TableColumn<Transaction, String> colTxn = new TableColumn<>("Transaction ID");
        colTxn.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        colTxn.setPrefWidth(220);

        TableColumn<Transaction, Integer> colSess = new TableColumn<>("Session ID");
        colSess.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
        colSess.setPrefWidth(90);

        TableColumn<Transaction, Double> colAmt = new TableColumn<>("Amount ($)");
        colAmt.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmt.setPrefWidth(100);

        TableColumn<Transaction, String> colMeth = new TableColumn<>("Payment Method");
        colMeth.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colMeth.setPrefWidth(140);

        TableColumn<Transaction, PaymentStatus> colStat = new TableColumn<>("Status");
        colStat.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        colStat.setPrefWidth(100);

        TableColumn<Transaction, String> colTime = new TableColumn<>("Timestamp");
        colTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colTime.setPrefWidth(160);

        table.getColumns().addAll(colTxn, colSess, colAmt, colMeth, colStat, colTime);

        try {
            var txns = billingService.getUserTransactions(currentUser.getId());
            table.setItems(FXCollections.observableArrayList(txns));
        } catch (SQLException ignored) {}

        box.getChildren().addAll(walletRow, lblTxnTable, table);
        setContent(box);
    }

    private void setContent(VBox view) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
    }

    public Parent getView() {
        return root;
    }
}
