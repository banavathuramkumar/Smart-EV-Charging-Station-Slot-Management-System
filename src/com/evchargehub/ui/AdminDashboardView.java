package com.evchargehub.ui;

import com.evchargehub.dao.*;
import com.evchargehub.exceptions.StationException;
import com.evchargehub.model.*;
import com.evchargehub.service.AuthenticationService;
import com.evchargehub.service.BookingService;
import com.evchargehub.service.StationService;
import com.evchargehub.util.ReportGenerator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Enterprise Administrator Dashboard for EV ChargeHub.
 * Demonstrates:
 * - Module 9: Admin Dashboard with live SQLite statistics
 * - Module 10: File handling with FileInputStream / FileOutputStream
 * - JavaFX GUI Controls, MenuBar, ToolBar, Tables, Dialogs
 */
public class AdminDashboardView {

    private final BorderPane root;
    private final StackPane contentArea;

    private final UserDAO userDAO;
    private final VehicleDAO vehicleDAO;
    private final StationDAO stationDAO;
    private final SlotDAO slotDAO;
    private final BookingDAO bookingDAO;
    private final SessionDAO sessionDAO;
    private final TransactionDAO transactionDAO;
    private final StationService stationService;
    private final BookingService bookingService;

    private Admin currentAdmin;

    public AdminDashboardView() {
        this.root = new BorderPane();
        this.contentArea = new StackPane();

        this.userDAO = new UserDAO();
        this.vehicleDAO = new VehicleDAO();
        this.stationDAO = new StationDAO();
        this.slotDAO = new SlotDAO();
        this.bookingDAO = new BookingDAO();
        this.sessionDAO = new SessionDAO();
        this.transactionDAO = new TransactionDAO();
        this.stationService = new StationService();
        this.bookingService = new BookingService();

        Person p = AuthenticationService.getCurrentUser();
        if (p instanceof Admin a) {
            this.currentAdmin = a;
        } else {
            this.currentAdmin = new Admin("admin", "", "Administrator", "", "");
        }

        buildUI();
    }

    private void buildUI() {
        root.setStyle("-fx-background-color: #0b1120;");

        // Top Header
        VBox topHeader = buildTopHeader();
        root.setTop(topHeader);

        // Sidebar
        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        // Content Area
        contentArea.setPadding(new Insets(24));
        root.setCenter(contentArea);

        showOverviewScreen();
    }

    private VBox buildTopHeader() {
        VBox header = new VBox();

        MenuBar menuBar = new MenuBar();
        Menu menuSystem = new Menu("System");
        MenuItem mRefresh = new MenuItem("🔄 Refresh SQLite Telemetry");
        mRefresh.setOnAction(e -> showOverviewScreen());
        MenuItem mLogout = new MenuItem("🚪 Sign Out");
        mLogout.setOnAction(e -> {
            AuthenticationService.logout();
            ViewNavigator.showLogin();
        });
        MenuItem mExit = new MenuItem("❌ Exit Application");
        mExit.setOnAction(e -> Platform.exit());
        menuSystem.getItems().addAll(mRefresh, new SeparatorMenuItem(), mLogout, mExit);

        Menu menuAdmin = new Menu("Operations");
        MenuItem mStations = new MenuItem("⚡ Manage Stations");
        mStations.setOnAction(e -> showStationsScreen());
        MenuItem mSlots = new MenuItem("🔌 Manage Slots");
        mSlots.setOnAction(e -> showSlotsScreen());
        MenuItem mUsers = new MenuItem("👥 Manage Users");
        mUsers.setOnAction(e -> showUsersScreen());
        MenuItem mBookings = new MenuItem("📋 Manage Bookings");
        mBookings.setOnAction(e -> showBookingsScreen());
        MenuItem mReports = new MenuItem("📄 Reports & Audit");
        mReports.setOnAction(e -> showReportsScreen());
        menuAdmin.getItems().addAll(mStations, mSlots, mUsers, mBookings, mReports);

        menuBar.getMenus().addAll(menuSystem, menuAdmin);

        ToolBar toolBar = new ToolBar();
        toolBar.setStyle("-fx-background-color: #131d31; -fx-padding: 6 16 6 16;");

        Button btnQuickStation = new Button("➕ Add Station");
        btnQuickStation.getStyleClass().add("btn-primary");
        btnQuickStation.setStyle("-fx-font-size: 11px; -fx-padding: 5 12 5 12;");
        btnQuickStation.setOnAction(e -> showStationsScreen());

        Button btnExportReport = new Button("📄 Generate Reports");
        btnExportReport.getStyleClass().add("btn-secondary");
        btnExportReport.setStyle("-fx-font-size: 11px; -fx-padding: 5 12 5 12;");
        btnExportReport.setOnAction(e -> showReportsScreen());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label adminBadge = new Label("🛡️ ADMIN: " + currentAdmin.getFullName() + " [Level " + currentAdmin.getAccessLevel() + "]");
        adminBadge.setStyle("-fx-text-fill: #34d399; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 4 10 4 10; -fx-background-color: rgba(16, 185, 129, 0.15); -fx-background-radius: 6;");

        toolBar.getItems().addAll(btnQuickStation, btnExportReport, new Separator(), spacer, adminBadge);

        header.getChildren().addAll(menuBar, toolBar);
        return header;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(20, 12, 20, 12));
        sidebar.getStyleClass().add("sidebar");

        Label brand = new Label("🛡️ Admin Console");
        brand.getStyleClass().add("sidebar-title");
        Label subtitle = new Label("STATION NETWORK CONTROL");
        subtitle.getStyleClass().add("sidebar-subtitle");
        VBox headerBox = new VBox(2, brand, subtitle);
        headerBox.setPadding(new Insets(0, 0, 16, 8));

        Button btnHome = createNavBtn("📊  Overview & KPIs", e -> showOverviewScreen());
        Button btnStations = createNavBtn("⚡  Station Management", e -> showStationsScreen());
        Button btnSlots = createNavBtn("🔌  Slot Management", e -> showSlotsScreen());
        Button btnUsers = createNavBtn("👥  User Directory", e -> showUsersScreen());
        Button btnBookings = createNavBtn("📋  Network Bookings", e -> showBookingsScreen());
        Button btnSessions = createNavBtn("🔋  Charging Sessions", e -> showSessionsScreen());
        Button btnReports = createNavBtn("📄  Audit & Reports (I/O)", e -> showReportsScreen());

        Region vSpacer = new Region();
        VBox.setVgrow(vSpacer, Priority.ALWAYS);

        Button btnLogout = new Button("🚪  Sign Out");
        btnLogout.getStyleClass().add("btn-danger");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setOnAction(e -> {
            AuthenticationService.logout();
            ViewNavigator.showLogin();
        });

        sidebar.getChildren().addAll(headerBox, new Separator(), btnHome, btnStations, btnSlots, btnUsers, btnBookings, btnSessions, btnReports, vSpacer, btnLogout);
        return sidebar;
    }

    private Button createNavBtn(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(handler);
        return btn;
    }

    // ==========================================
    // SCREEN 1: Overview & Live KPIs from SQLite
    // ==========================================
    private void showOverviewScreen() {
        VBox box = new VBox(20);
        box.getChildren().add(UITheme.createSectionHeader("System Telemetry & Network Overview",
                "Real-time operational metrics queried dynamically from SQLite database tables."));

        // Live KPI Grid
        int totalUsers = 0, totalVehicles = 0, totalStations = 0, totalSlots = 0, availSlots = 0, activeSessions = 0, totalBookings = 0, completedSessions = 0;
        double totalRevenue = 0.0;

        try {
            totalUsers = userDAO.countTotalUsers();
            totalVehicles = vehicleDAO.countTotalVehicles();
            totalStations = stationDAO.countTotalStations();
            totalSlots = slotDAO.countTotalSlots();
            availSlots = slotDAO.countAvailableSlots();
            activeSessions = sessionDAO.countActiveSessions();
            totalBookings = bookingDAO.countTotalBookings();
            completedSessions = sessionDAO.countCompletedSessions();
            totalRevenue = transactionDAO.calculateTotalRevenue();
        } catch (SQLException ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "DB Error", ex.getMessage());
        }

        // First Row KPIs
        HBox row1 = new HBox(16);
        row1.getChildren().addAll(
            UITheme.createStatCard("TOTAL REVENUE", "$" + String.format("%.2f", totalRevenue), "From Paid Sessions", "#10b981"),
            UITheme.createStatCard("ACTIVE SESSIONS", String.valueOf(activeSessions), "Currently Charging", "#38bdf8"),
            UITheme.createStatCard("AVAILABLE SLOTS", availSlots + " / " + totalSlots, "Ready for Booking", "#fbbf24"),
            UITheme.createStatCard("TOTAL BOOKINGS", String.valueOf(totalBookings), "All-time Reservations", "#a78bfa")
        );

        // Second Row KPIs
        HBox row2 = new HBox(16);
        row2.getChildren().addAll(
            UITheme.createStatCard("CHARGING STATIONS", String.valueOf(totalStations), "Installed Hubs", "#34d399"),
            UITheme.createStatCard("REGISTERED USERS", String.valueOf(totalUsers), "EV Drivers", "#60a5fa"),
            UITheme.createStatCard("EV VEHICLES", String.valueOf(totalVehicles), "Linked Fleet", "#f472b6"),
            UITheme.createStatCard("COMPLETED SESSIONS", String.valueOf(completedSessions), "Finished Cycles", "#4ade80")
        );

        // Quick Admin Actions
        VBox actionsCard = new VBox(12);
        actionsCard.getStyleClass().add("card");
        Label lblQuick = new Label("⚡ Quick Station Network Operations");
        lblQuick.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #ffffff;");

        HBox btnRow = new HBox(12);
        Button b1 = new Button("⚡ Add New Station");
        b1.getStyleClass().add("btn-primary");
        b1.setOnAction(e -> showStationsScreen());

        Button b2 = new Button("🔌 Configure Slots");
        b2.getStyleClass().add("btn-secondary");
        b2.setOnAction(e -> showSlotsScreen());

        Button b3 = new Button("📄 Export Daily Booking Report");
        b3.getStyleClass().add("btn-secondary");
        b3.setOnAction(e -> showReportsScreen());

        btnRow.getChildren().addAll(b1, b2, b3);
        actionsCard.getChildren().addAll(lblQuick, btnRow);

        box.getChildren().addAll(row1, row2, actionsCard);
        setContent(box);
    }

    // ==========================================
    // SCREEN 2: Station Management
    // ==========================================
    private void showStationsScreen() {
        VBox box = new VBox(16);
        box.getChildren().add(UITheme.createSectionHeader("Charging Station Management",
                "Create, update, activate, deactivate, or delete physical station hubs."));

        TableView<ChargingStation> table = new TableView<>();
        table.setPrefHeight(340);

        TableColumn<ChargingStation, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("stationId"));
        colId.setPrefWidth(50);

        TableColumn<ChargingStation, String> colName = new TableColumn<>("Station Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setPrefWidth(180);

        TableColumn<ChargingStation, String> colLoc = new TableColumn<>("Location");
        colLoc.setCellValueFactory(new PropertyValueFactory<>("location"));
        colLoc.setPrefWidth(180);

        TableColumn<ChargingStation, Integer> colTotal = new TableColumn<>("Total Slots");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalSlots"));
        colTotal.setPrefWidth(90);

        TableColumn<ChargingStation, Integer> colAvail = new TableColumn<>("Available");
        colAvail.setCellValueFactory(new PropertyValueFactory<>("availableSlots"));
        colAvail.setPrefWidth(90);

        TableColumn<ChargingStation, Double> colSpeed = new TableColumn<>("Speed (kW)");
        colSpeed.setCellValueFactory(new PropertyValueFactory<>("chargingSpeedKW"));
        colSpeed.setPrefWidth(90);

        TableColumn<ChargingStation, Double> colPrice = new TableColumn<>("Price/kWh ($)");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("pricePerKWh"));
        colPrice.setPrefWidth(100);

        TableColumn<ChargingStation, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(90);

        table.getColumns().addAll(colId, colName, colLoc, colTotal, colAvail, colSpeed, colPrice, colStatus);

        try {
            table.setItems(FXCollections.observableArrayList(stationService.getAllStations()));
        } catch (SQLException ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "DB Error", ex.getMessage());
        }

        // Add Station Form
        VBox formCard = new VBox(12);
        formCard.getStyleClass().add("card");
        Label lblForm = new Label("➕ Add New Charging Station");
        lblForm.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");

        HBox inputs = new HBox(10);
        TextField txtName = new TextField();
        txtName.setPromptText("Station Name (e.g. Mangalagiri Fast Hub)");

        TextField txtLoc = new TextField();
        txtLoc.setPromptText("Location (e.g. NH16 Bypass)");

        TextField txtSpeed = new TextField("60.0");
        txtSpeed.setPromptText("Speed kW");
        txtSpeed.setPrefWidth(90);

        TextField txtPrice = new TextField("14.50");
        txtPrice.setPromptText("Price/kWh");
        txtPrice.setPrefWidth(90);

        inputs.getChildren().addAll(txtName, txtLoc, txtSpeed, txtPrice);

        HBox actions = new HBox(12);
        Button btnAdd = new Button("Save Station");
        btnAdd.getStyleClass().add("btn-primary");
        btnAdd.setOnAction(e -> {
            try {
                double speed = Double.parseDouble(txtSpeed.getText().trim());
                double price = Double.parseDouble(txtPrice.getText().trim());
                stationService.addStation(txtName.getText().trim(), txtLoc.getText().trim(), speed, price);
                showStationsScreen();
            } catch (StationException | SQLException | NumberFormatException ex) {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Error", ex.getMessage());
            }
        });

        Button btnToggle = new Button("🔄 Toggle Active/Inactive");
        btnToggle.getStyleClass().add("btn-secondary");
        btnToggle.setOnAction(e -> {
            ChargingStation s = table.getSelectionModel().getSelectedItem();
            if (s != null) {
                try {
                    stationService.toggleStationStatus(s.getStationId());
                    showStationsScreen();
                } catch (SQLException ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
                }
            } else {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection Required", "Select a station to toggle status.");
            }
        });

        Button btnDelete = new Button("🗑️ Delete Station");
        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setOnAction(e -> {
            ChargingStation s = table.getSelectionModel().getSelectedItem();
            if (s != null) {
                if (ViewNavigator.showConfirmation("Delete Station", "Delete " + s.getName() + " and all its associated slots?")) {
                    try {
                        stationService.deleteStation(s.getStationId());
                        showStationsScreen();
                    } catch (StationException | SQLException ex) {
                        ViewNavigator.showAlert(Alert.AlertType.WARNING, "Notice", ex.getMessage());
                    }
                }
            } else {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a station from the table to delete.");
            }
        });

        actions.getChildren().addAll(btnAdd, btnToggle, btnDelete);
        formCard.getChildren().addAll(lblForm, inputs, actions);

        box.getChildren().addAll(table, formCard);
        setContent(box);
    }

    // ==========================================
    // SCREEN 3: Slot Management
    // ==========================================
    private void showSlotsScreen() {
        VBox box = new VBox(16);
        box.getChildren().add(UITheme.createSectionHeader("Charging Slot Management",
                "Configure individual charging connectors, power delivery (kW), and maintenance states."));

        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        Label lblStn = new Label("Select Station:");
        lblStn.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: 600;");

        ComboBox<ChargingStation> cmbStation = new ComboBox<>();
        try {
            var stns = stationService.getAllStations();
            cmbStation.setItems(FXCollections.observableArrayList(stns));
            if (!stns.isEmpty()) cmbStation.setValue(stns.get(0));
        } catch (SQLException ignored) {}

        TableView<ChargingSlot> table = new TableView<>();
        table.setPrefHeight(340);

        TableColumn<ChargingSlot, Integer> colId = new TableColumn<>("Slot ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("slotId"));
        colId.setPrefWidth(80);

        TableColumn<ChargingSlot, String> colNum = new TableColumn<>("Slot Number");
        colNum.setCellValueFactory(new PropertyValueFactory<>("slotNumber"));
        colNum.setPrefWidth(120);

        TableColumn<ChargingSlot, String> colConn = new TableColumn<>("Connector Type");
        colConn.setCellValueFactory(new PropertyValueFactory<>("connectorType"));
        colConn.setPrefWidth(140);

        TableColumn<ChargingSlot, Double> colPower = new TableColumn<>("Power (kW)");
        colPower.setCellValueFactory(new PropertyValueFactory<>("chargingPowerKW"));
        colPower.setPrefWidth(100);

        TableColumn<ChargingSlot, SlotStatus> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(140);

        table.getColumns().addAll(colId, colNum, colConn, colPower, colStatus);

        Runnable loadSlots = () -> {
            ChargingStation s = cmbStation.getValue();
            if (s != null) {
                try {
                    table.setItems(FXCollections.observableArrayList(stationService.getSlotsByStation(s.getStationId())));
                } catch (SQLException ex) {
                    ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
                }
            }
        };

        cmbStation.setOnAction(e -> loadSlots.run());
        loadSlots.run();

        filterBar.getChildren().addAll(lblStn, cmbStation);

        // Add Slot Card
        VBox addCard = new VBox(12);
        addCard.getStyleClass().add("card");
        Label lblAdd = new Label("➕ Add Charging Slot to Selected Station");
        lblAdd.setStyle("-fx-font-weight: bold; -fx-text-fill: #ffffff;");

        HBox inputs = new HBox(10);
        TextField txtSlotNum = new TextField();
        txtSlotNum.setPromptText("Slot No (e.g. SLOT-A5)");

        ComboBox<String> cmbConn = new ComboBox<>();
        cmbConn.getItems().addAll("CCS2", "Type 2", "CHAdeMO", "GB/T");
        cmbConn.setValue("CCS2");

        TextField txtPower = new TextField("60.0");
        txtPower.setPromptText("Power kW");
        txtPower.setPrefWidth(100);

        inputs.getChildren().addAll(txtSlotNum, cmbConn, txtPower);

        HBox actions = new HBox(12);
        Button btnSaveSlot = new Button("Add Slot");
        btnSaveSlot.getStyleClass().add("btn-primary");
        btnSaveSlot.setOnAction(e -> {
            ChargingStation s = cmbStation.getValue();
            if (s != null) {
                try {
                    double p = Double.parseDouble(txtPower.getText().trim());
                    stationService.addSlot(s.getStationId(), txtSlotNum.getText().trim(), cmbConn.getValue(), p);
                    loadSlots.run();
                } catch (StationException | SQLException | NumberFormatException ex) {
                    ViewNavigator.showAlert(Alert.AlertType.WARNING, "Error", ex.getMessage());
                }
            }
        });

        Button btnSetAvail = new Button("Mark AVAILABLE");
        btnSetAvail.getStyleClass().add("btn-secondary");
        btnSetAvail.setOnAction(e -> updateSelectedSlotStatus(table, cmbStation.getValue(), SlotStatus.AVAILABLE, loadSlots));

        Button btnSetMaint = new Button("Mark MAINTENANCE");
        btnSetMaint.getStyleClass().add("btn-secondary");
        btnSetMaint.setOnAction(e -> updateSelectedSlotStatus(table, cmbStation.getValue(), SlotStatus.MAINTENANCE, loadSlots));

        Button btnDeleteSlot = new Button("🗑️ Delete Slot");
        btnDeleteSlot.getStyleClass().add("btn-danger");
        btnDeleteSlot.setOnAction(e -> {
            ChargingSlot slot = table.getSelectionModel().getSelectedItem();
            ChargingStation s = cmbStation.getValue();
            if (slot != null && s != null) {
                if (ViewNavigator.showConfirmation("Delete Slot", "Delete " + slot.getSlotNumber() + "?")) {
                    try {
                        stationService.deleteSlot(slot.getSlotId(), s.getStationId());
                        loadSlots.run();
                    } catch (SQLException ex) {
                        ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
                    }
                }
            } else {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a slot from the table to delete.");
            }
        });

        actions.getChildren().addAll(btnSaveSlot, btnSetAvail, btnSetMaint, btnDeleteSlot);
        addCard.getChildren().addAll(lblAdd, inputs, actions);

        box.getChildren().addAll(filterBar, table, addCard);
        setContent(box);
    }

    private void updateSelectedSlotStatus(TableView<ChargingSlot> table, ChargingStation s, SlotStatus status, Runnable callback) {
        ChargingSlot slot = table.getSelectionModel().getSelectedItem();
        if (slot != null && s != null) {
            try {
                stationService.updateSlotStatus(slot.getSlotId(), status, s.getStationId());
                callback.run();
            } catch (SQLException ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        } else {
            ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a slot.");
        }
    }

    // ==========================================
    // SCREEN 4: User Directory
    // ==========================================
    private void showUsersScreen() {
        VBox box = new VBox(16);
        box.getChildren().add(UITheme.createSectionHeader("Registered User Directory",
                "View registered EV drivers, system administrators, and contact details."));

        TableView<Person> table = new TableView<>();
        table.setPrefHeight(450);

        TableColumn<Person, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<Person, String> colUser = new TableColumn<>("Username");
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colUser.setPrefWidth(120);

        TableColumn<Person, String> colName = new TableColumn<>("Full Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colName.setPrefWidth(160);

        TableColumn<Person, String> colEmail = new TableColumn<>("Email Address");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(180);

        TableColumn<Person, String> colPhone = new TableColumn<>("Phone");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPhone.setPrefWidth(120);

        TableColumn<Person, String> colRole = new TableColumn<>("Role");
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colRole.setPrefWidth(90);

        table.getColumns().addAll(colId, colUser, colName, colEmail, colPhone, colRole);

        try {
            table.setItems(FXCollections.observableArrayList(userDAO.findAll()));
        } catch (SQLException ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "DB Error", ex.getMessage());
        }

        Button btnDeleteUser = new Button("🗑️ Delete Selected User Account");
        btnDeleteUser.getStyleClass().add("btn-danger");
        btnDeleteUser.setOnAction(e -> {
            Person selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if ("admin".equalsIgnoreCase(selected.getUsername())) {
                    ViewNavigator.showAlert(Alert.AlertType.WARNING, "Protected", "Primary admin account cannot be deleted.");
                    return;
                }
                if (ViewNavigator.showConfirmation("Delete User", "Delete user " + selected.getUsername() + " and all their bookings?")) {
                    try {
                        userDAO.delete(selected.getId());
                        showUsersScreen();
                    } catch (SQLException ex) {
                        ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
                    }
                }
            } else {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a user account to delete.");
            }
        });

        box.getChildren().addAll(table, btnDeleteUser);
        setContent(box);
    }

    // ==========================================
    // SCREEN 5: Network Bookings
    // ==========================================
    private void showBookingsScreen() {
        VBox box = new VBox(16);
        box.getChildren().add(UITheme.createSectionHeader("Network-Wide Slot Reservations",
                "Full ledger of upcoming, confirmed, in-progress, and completed bookings across all stations."));

        TableView<Booking> table = new TableView<>();
        table.setPrefHeight(450);

        TableColumn<Booking, Integer> colId = new TableColumn<>("Booking ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colId.setPrefWidth(80);

        TableColumn<Booking, String> colUser = new TableColumn<>("Driver");
        colUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colUser.setPrefWidth(140);

        TableColumn<Booking, String> colStn = new TableColumn<>("Station");
        colStn.setCellValueFactory(new PropertyValueFactory<>("stationName"));
        colStn.setPrefWidth(180);

        TableColumn<Booking, String> colSlot = new TableColumn<>("Slot");
        colSlot.setCellValueFactory(new PropertyValueFactory<>("slotNumber"));
        colSlot.setPrefWidth(80);

        TableColumn<Booking, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        colDate.setPrefWidth(100);

        TableColumn<Booking, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colTime.setPrefWidth(70);

        TableColumn<Booking, String> colEnd = new TableColumn<>("End");
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colEnd.setPrefWidth(70);

        TableColumn<Booking, BookingStatus> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(110);

        table.getColumns().addAll(colId, colUser, colStn, colSlot, colDate, colTime, colEnd, colStatus);

        try {
            table.setItems(FXCollections.observableArrayList(bookingService.getAllBookings()));
        } catch (SQLException ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "DB Error", ex.getMessage());
        }

        Button btnCancel = new Button("Admin Cancel Booking");
        btnCancel.getStyleClass().add("btn-danger");
        btnCancel.setOnAction(e -> {
            Booking b = table.getSelectionModel().getSelectedItem();
            if (b != null) {
                if (ViewNavigator.showConfirmation("Admin Cancellation", "Force cancel Booking #" + b.getBookingId() + "?")) {
                    try {
                        bookingService.cancelBooking(b.getBookingId());
                        showBookingsScreen();
                    } catch (SQLException ex) {
                        ViewNavigator.showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
                    }
                }
            } else {
                ViewNavigator.showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a booking to cancel.");
            }
        });

        box.getChildren().addAll(table, btnCancel);
        setContent(box);
    }

    // ==========================================
    // SCREEN 6: Charging Sessions & Telemetry
    // ==========================================
    private void showSessionsScreen() {
        VBox box = new VBox(16);
        box.getChildren().add(UITheme.createSectionHeader("Charging Sessions & Power Telemetry",
                "Log of all charging cycles, energy delivered (kWh), and generated charges."));

        TableView<ChargingSession> table = new TableView<>();
        table.setPrefHeight(450);

        TableColumn<ChargingSession, Integer> colId = new TableColumn<>("Session ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
        colId.setPrefWidth(90);

        TableColumn<ChargingSession, Integer> colBook = new TableColumn<>("Booking ID");
        colBook.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        colBook.setPrefWidth(90);

        TableColumn<ChargingSession, Integer> colUser = new TableColumn<>("User ID");
        colUser.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUser.setPrefWidth(80);

        TableColumn<ChargingSession, String> colStart = new TableColumn<>("Start Time");
        colStart.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colStart.setPrefWidth(110);

        TableColumn<ChargingSession, String> colEnd = new TableColumn<>("End Time");
        colEnd.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colEnd.setPrefWidth(110);

        TableColumn<ChargingSession, Double> colEnergy = new TableColumn<>("Energy (kWh)");
        colEnergy.setCellValueFactory(new PropertyValueFactory<>("energyDeliveredKWh"));
        colEnergy.setPrefWidth(110);

        TableColumn<ChargingSession, Double> colCost = new TableColumn<>("Total Cost ($)");
        colCost.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        colCost.setPrefWidth(110);

        TableColumn<ChargingSession, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setPrefWidth(110);

        table.getColumns().addAll(colId, colBook, colUser, colStart, colEnd, colEnergy, colCost, colStatus);

        try {
            table.setItems(FXCollections.observableArrayList(sessionDAO.findAll()));
        } catch (SQLException ex) {
            ViewNavigator.showAlert(Alert.AlertType.ERROR, "DB Error", ex.getMessage());
        }

        box.getChildren().add(table);
        setContent(box);
    }

    // ==========================================
    // SCREEN 7: Reports & File Handling (Java I/O Streams)
    // ==========================================
    private void showReportsScreen() {
        VBox box = new VBox(16);
        box.getChildren().add(UITheme.createSectionHeader("Reports & File Handling (Java I/O)",
                "Generates CSV and Text audit reports using FileOutputStream and reads them back via FileInputStream."));

        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        TextArea txtReportViewer = new TextArea();
        txtReportViewer.setEditable(false);
        txtReportViewer.setPrefHeight(380);
        txtReportViewer.setStyle("-fx-font-family: monospace; -fx-font-size: 12px; -fx-background-color: #0f172a; -fx-text-fill: #34d399;");
        txtReportViewer.setText("Select a report button above to generate a new report using FileOutputStream and preview it here via FileInputStream.");

        Button btnBookingsReport = new Button("📄 Generate Booking Report (CSV)");
        btnBookingsReport.getStyleClass().add("btn-primary");
        btnBookingsReport.setOnAction(e -> {
            try {
                var bookings = bookingDAO.findAll();
                File f = ReportGenerator.generateBookingReport(bookings);
                String content = ReportGenerator.readReportContent(f);
                txtReportViewer.setText("=== BOOKINGS REPORT GENERATED SUCCESSFULLY ===\nFile: " + f.getAbsolutePath() + "\n\n" + content);
                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Report Generated", "Booking report exported to:\n" + f.getAbsolutePath());
            } catch (IOException | SQLException ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Report Error", ex.getMessage());
            }
        });

        Button btnRevenueReport = new Button("💰 Generate Revenue Report (CSV)");
        btnRevenueReport.getStyleClass().add("btn-primary");
        btnRevenueReport.setOnAction(e -> {
            try {
                var txns = transactionDAO.findAll();
                double rev = transactionDAO.calculateTotalRevenue();
                File f = ReportGenerator.generateRevenueReport(txns, rev);
                String content = ReportGenerator.readReportContent(f);
                txtReportViewer.setText("=== REVENUE REPORT GENERATED SUCCESSFULLY ===\nFile: " + f.getAbsolutePath() + "\n\n" + content);
                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Report Generated", "Revenue report exported to:\n" + f.getAbsolutePath());
            } catch (IOException | SQLException ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Report Error", ex.getMessage());
            }
        });

        Button btnStationReport = new Button("⚡ Generate Station Utilization (TXT)");
        btnStationReport.getStyleClass().add("btn-primary");
        btnStationReport.setOnAction(e -> {
            try {
                var stns = stationDAO.findAll();
                File f = ReportGenerator.generateStationReport(stns);
                String content = ReportGenerator.readReportContent(f);
                txtReportViewer.setText("=== STATION UTILIZATION REPORT GENERATED SUCCESSFULLY ===\nFile: " + f.getAbsolutePath() + "\n\n" + content);
                ViewNavigator.showAlert(Alert.AlertType.INFORMATION, "Report Generated", "Station report exported to:\n" + f.getAbsolutePath());
            } catch (IOException | SQLException ex) {
                ViewNavigator.showAlert(Alert.AlertType.ERROR, "Report Error", ex.getMessage());
            }
        });

        btnRow.getChildren().addAll(btnBookingsReport, btnRevenueReport, btnStationReport);
        box.getChildren().addAll(btnRow, txtReportViewer);
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
