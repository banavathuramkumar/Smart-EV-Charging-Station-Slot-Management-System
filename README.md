# EV ChargeHub – Smart EV Charging Station & Slot Management System

[![Java](https://img.shields.io/badge/Java-21%2B%20%2F%2026-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.2-orange?style=for-the-badge)](https://openjfx.io/)
[![SQLite](https://img.shields.io/badge/SQLite-3.45.2.0-003B57?style=for-the-badge&logo=sqlite&logoColor=white)](https://www.sqlite.org/)
[![VS Code](https://img.shields.io/badge/VS_Code-Ready-007ACC?style=for-the-badge&logo=visual-studio-code&logoColor=white)](https://code.visualstudio.com/)

**Domain:** Smart EV Charging Station & Slot Management Network (Vijayawada to Hyderabad NH 65 Corridor)  
**Technology:** Core Java, JavaFX, SQLite JDBC, Multithreading, Generics, Byte Streams  

---

## 📖 Project Overview

**EV ChargeHub** is a production-grade Java desktop application designed to streamline electric vehicle (EV) charging station operations and smart slot reservations. The system implements:

- **Role-Based Authentication:** Distinct interactive dashboards for EV Drivers and Station Network Administrators.
- **Highway Station Network:** Comprehensive coverage of 12 fast-charging hubs along the Vijayawada to Hyderabad corridor (NH 65).
- **Smart Slot Booking:** Prevents double-booking through Java monitor synchronization and atomic SQLite transactions.
- **Multithreaded Simulation:** Real-time charging cycle simulation using a background `Thread` with safe JavaFX `Platform.runLater` UI updates.
- **Waiting Queue:** First-In, First-Out (FIFO) queue using `LinkedList` with inter-thread communication (`wait()` and `notifyAll()`).
- **Billing & Wallet:** Instant simulated payment calculation and transaction history ledger.
- **Audit & Reporting:** Java I/O Byte Streams (`FileOutputStream`, `FileInputStream`) for exporting CSV and text reports.

---

## 🛠️ Technology Stack & Dependencies

| Layer | Component | Details |
| :--- | :--- | :--- |
| **Language** | Core Java | JDK 21+ (tested on JDK 26.0.1) |
| **GUI Framework** | JavaFX | OpenJFX SDK 21.0.2 (`lib/javafx-sdk-21.0.2/`) |
| **Database** | SQLite & JDBC | `lib/sqlite-jdbc-3.45.2.0.jar` |
| **Persistence** | Relational DB | `data/ev_chargehub.db` (auto-provisioned) |
| **Build / IDE** | Turnkey Scripts | `.vscode/launch.json`, `build.bat`, `run.bat` |

> [!NOTE]
> All necessary JavaFX SDK components and SQLite JDBC driver JARs are already included in the `lib/` folder. **No external installation of Maven or Gradle is required.**

---

## 🚀 Quick Start Guide

### Option 1: Run with VS Code (Recommended)
1. Open this project folder in Visual Studio Code.
2. Ensure the **Extension Pack for Java** is installed.
3. Open `src/com/evchargehub/Main.java`.
4. Press **`F5`** (or click **Run > Start Debugging**).

### Option 2: Run via Command Prompt or PowerShell

#### 1. Compile the Project:
Using PowerShell:
```powershell
.\build.ps1
```
Using Command Prompt:
```cmd
build.bat
```

#### 2. Launch the Application:
Using PowerShell:
```powershell
.\run.ps1
```
Using Command Prompt:
```cmd
run.bat
```

---

## 🔑 Default Accounts (First-Run Credentials)

The application automatically provisions the SQLite database (`data/ev_chargehub.db`) and seeds 12 charging stations and default driver accounts on first run:

| Role | Username | Password | Access Capabilities |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `Admin@123` | Full network control: Stations, Slots, Users, Bookings, Sessions, I/O Reports. |
| **EV Driver User** | `driver_user` | `User@123` | Reserve slots, simulate charging, check wallet ($150 balance), view history. |
| **EV Driver User 2** | `priya_reddy` | `User@123` | Multi-user booking testing with MG ZS EV ($180 balance). |

> [!TIP]
> You can also click **"Register New Account"** on the login screen to create a brand new driver account with a complimentary $100.00 wallet credit.

---

## 📁 Project Directory Structure

```
Project Java/
├── .vscode/
│   ├── launch.json              # VS Code Java run configurations
│   ├── settings.json            # Java classpath and library references
│   └── tasks.json               # VS Code build and test tasks
├── lib/
│   ├── javafx-sdk-21.0.2/       # OpenJFX SDK binaries & JARs
│   ├── sqlite-jdbc-3.45.2.0.jar # SQLite JDBC Driver
│   ├── slf4j-api.jar            # Logging bridge
│   └── slf4j-simple.jar
├── src/
│   └── com/evchargehub/
│       ├── Main.java            # JavaFX Application Entry Point
│       ├── BackendTest.java     # Headless Integration Test Suite
│       ├── ConcurrencyTest.java # 5-Thread Atomic Race Condition Test
│       ├── JavaFXSmokeTest.java # GUI Smoke Test
│       ├── model/               # Object-Oriented Domain Entities
│       ├── dao/                 # SQLite JDBC Data Access Objects
│       ├── database/            # Connection & Schema Initializer
│       ├── service/             # Business Logic & Validation
│       ├── threads/             # Multithreaded Charging & Queue Workers
│       ├── collections/         # Collections Framework & Generics
│       ├── events/              # AWT Event Delegation & Adapters
│       ├── exceptions/          # Custom User-Defined Exceptions
│       ├── util/                # Security Hashing & Byte Stream I/O
│       └── ui/                  # JavaFX Responsive Dark UI & Dashboards
├── data/
│   └── ev_chargehub.db          # Auto-generated SQLite Database
├── reports/                     # Output directory for CSV and TXT audit reports
├── build.ps1 / build.bat        # Automated Compilation Scripts
├── run.ps1 / run.bat            # Application Launchers
└── README.md                    # Project Documentation
```

---

## 🧪 Automated Testing & Verification

### 1. Headless Integration Test Suite
Tests SQLite persistence, authentication, station queries, collections, and byte-stream report generation without opening a GUI window:
```cmd
java -cp "bin;lib\*" com.evchargehub.BackendTest
```

### 2. Concurrency & Race Condition Verification
Simultaneously fires 5 client threads targeting the same slot:
```cmd
java -cp "bin;lib\*" com.evchargehub.ConcurrencyTest
```
*(Result: Exactly 1 thread succeeds; 4 threads blocked by synchronization lock).*

---

## 📑 Core Java Feature Traceability Matrix

| Concept Area | Key Implementation Feature | Source File Reference |
| :--- | :--- | :--- |
| **Object-Oriented Programming** | Abstract class, multilevel hierarchy, `super`, `final` | `com.evchargehub.model.Person`, `User`, `Admin` |
| **Polymorphism & Exceptions** | Method overloading, custom exceptions, nested try | `Vehicle.java`, `com.evchargehub.exceptions.*` |
| **Multithreading & I/O** | `Thread`, `Runnable`, `wait/notify`, Byte Streams | `ChargingSessionThread.java`, `ReportGenerator.java` |
| **Database & Collections** | SQLite JDBC CRUD, `ArrayList`, `LinkedList`, `TreeSet` | `DatabaseInitializer.java`, `CollectionsDemo.java` |
| **Generics & JavaFX** | `<K,V>`, Wildcards, `MenuBar`, `ToolBar`, `TreeView` | `KeyValuePair.java`, `UserDashboardView.java` |

---

## 🛡️ Technology Standards & Architecture
This project was developed strictly using standard Java technologies. No prohibited external web frameworks (Spring Boot, Hibernate, Node.js, Web/REST APIs) were used. All dependencies are self-contained in `lib/`.
