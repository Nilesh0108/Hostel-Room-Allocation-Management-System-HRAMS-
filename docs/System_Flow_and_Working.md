# Hostel Room Allocation Management System (HRAMS)
## System Flow & Technical Working Guide

---

## 📌 Executive Summary

The **Hostel Room Allocation Management System (HRAMS)** is a desktop application designed for hostel administrators to replace manual, paper-based room tracking with an automated digital platform. 

The system automates student record management, room directory tracking, **First-Fit room allocation with multi-student roommate co-occupancy display**, **First-In-First-Out (FIFO) waiting list management**, room vacating workflows, and detailed room-sharing occupancy reporting.

---

## 🏗️ System Architecture Overview

HRAMS follows a clean, decoupled **Layered Architecture** with single-direction data flow:

```
+-----------------------------------------------------------------------+
|                         JavaFX Presentation Layer                     |
|            FXML Views (login, dashboard, students, rooms, etc.)       |
|                     & JavaFX Controller Classes                       |
+-----------------------------------------------------------------------+
                                   │
                                   ▼
+-----------------------------------------------------------------------+
|                             Service Layer                             |
|          AllocationService.java  (Orchestrates business logic)        |
+-----------------------------------------------------------------------+
                        │                       │
                        ▼                       ▼
+-------------------------------+   +-----------------------------------+
|       Algorithm Layer         |   |         Data Access Layer         |
|   AllocationAlgorithm.java    |   |  StudentDAO, RoomDAO, AdminDAO,   |
|   (First-Fit Room Search)     |   |   AllocationDAO, WaitingListDAO   |
+-------------------------------+   +-----------------------------------+
                                                │
                                                ▼
                                    +-----------------------+
                                    |     Database Layer    |
                                    |  MySQL / Embedded H2  |
                                    +-----------------------+
```

### Layer Breakdown:
1. **Presentation Layer (JavaFX / FXML / CSS)**: Handles the user interface, form inputs, button actions, high-contrast dark theme rendering, and table visual displays.
2. **Controller Layer**: Bridges FXML views with services. Executes long-running DB tasks asynchronously on background threads to keep the UI smooth and responsive.
3. **Service Layer (`AllocationService.java`)**: Implements business rules (e.g., single-allocation restriction per student, automatic queue placement when rooms are full, automatic multi-student queue fulfillment upon room vacancy or expansion).
4. **Algorithm Layer (`AllocationAlgorithm.java`)**: Implements the **First-Fit Algorithm** to find the first room with available bed capacity in $O(n)$ time.
5. **DAO Layer (Data Access Objects)**: Encapsulates raw JDBC SQL queries (`PreparedStatement`, `ResultSet`, multi-occupant aggregation).
6. **Database Layer (`DatabaseConnection.java`)**: Manages JDBC connections to **MySQL (`hrams_db`)** with instant failover to **Embedded H2 (`./data/hrams_db`)** if MySQL is not running.

---

## 🔄 Step-by-Step User & Technical Flow

```mermaid
graph TD
    A[Launch Application] --> B[Step 1: Admin Login Screen]
    B -->|Authenticate Credentials| C{Valid Admin?}
    C -->|No| B
    C -->|Yes| D[Step 2: Dashboard Overview]

    D --> E[Step 3: Student Management]
    D --> F[Step 4: Room Management]
    D --> G[Step 5: Room Allocation Engine]
    D --> H[Step 6: Waiting List Queue]
    D --> I[Step 7: Reports Summary]

    G --> J[Step 5.1: Select Student ID]
    J --> K{Already Has Active Allocation?}
    K -->|Yes| L[Reject: Student Already Allocated]
    K -->|No| M[Step 5.2: Execute First-Fit Algorithm]
    
    M --> N{Available Room Found?}
    N -->|Yes: Multi-Bed Room Free| O[Increment Occupancy, Update Roommate Roster & Create ALLOCATION]
    N -->|No: Hostel Full| P[Step 5.3: Queue Student to WAITING_LIST (FIFO)]

    Q[Step 6: Vacate Room / Expand Capacity] --> R[Set Status VACATED & Decrement Occupancy]
    R --> S{Student in Waiting List?}
    S -->|Yes| T[Automatically Allocate Room to Next FIFO Candidate up to Capacity]
    S -->|No| U[Room Remains Vacant]
```

---

### Step 1: Admin Login Flow

1. **User Action**: Admin launches the app and enters username (`admin`) and password (`admin123`).
2. **Technical Process**:
   - `LoginController` validates that input fields are non-empty.
   - Triggers an asynchronous thread calling `AdminDAO.authenticate(username, password)`.
   - `DatabaseConnection` tests connection to MySQL (`jdbc:mysql://localhost:3306/hrams_db`). If MySQL server is offline, it fails over to embedded H2 (`./data/hrams_db`) in **under 1 second**.
   - `AdminDAO` executes `SELECT * FROM ADMIN WHERE username = ? AND password = ?`.
3. **Outcome**:
   - On success: Saves `loggedInAdmin` session and navigates to the main layout (`main_layout.fxml`).
   - On failure: Displays an explicit red error message ("Invalid admin credentials").

---

### Step 2: Dashboard Overview & Multi-Student Roommate Sharing Flow

1. **User Action**: Navigates to the Dashboard screen.
2. **Technical Process**:
   - `DashboardController` calls `StudentDAO`, `RoomDAO`, `WaitingListDAO`, and `AllocationDAO` to query current database stats.
   - Calculates 4 key metrics:
     - **Total Students**: Total records in `STUDENT` table.
     - **Available Rooms**: Rooms where `occupied_count < capacity`.
     - **Full / Occupied Rooms**: Rooms where `occupied_count >= capacity`.
     - **Waiting Queue**: Pending entries in `WAITING_LIST` with status `'WAITING'`.
   - Populates `tblDashboardAllocations` with all current active room allocations, displaying:
     - **Room & Capacity**: Shows exact bed ratio e.g., `A-102 (2/2 Beds)` or `B-202 (1/3 Beds)`.
     - **Co-Occupants / Roommates**: Displays the complete roster of all students sharing that specific room (e.g., `John Doe, Jane Smith`).
3. **Outcome**: Renders real-time statistics cards and active allocations roommate sharing overview.

---

### Step 3: Student Management Flow

1. **User Action**: Admin adds, updates, deletes, or searches for a student.
2. **Technical Process**:
   - **Add**: Validates student ID uniqueness and inserts record (`student_id`, `name`, `email`, `phone`, `course`, `year_of_study`, `gender`) via `StudentDAO.addStudent()`.
   - **Update**: Modifies student details via `StudentDAO.updateStudent()`.
   - **Search**: `StudentDAO.searchStudents(query)` runs `LIKE %query%` against ID and Name.
   - **Delete**: Prompts confirmation dialog and calls `StudentDAO.deleteStudent()`.
3. **Outcome**: Table directory immediately updates showing formatted student entries.

---

### Step 4: Room Management Flow

1. **User Action**: Admin manages rooms (`room_number`, `capacity`, `room_type`, `floor`).
2. **Technical Process**:
   - **Add / Update**: Saves room details into `ROOM` table via `RoomDAO`.
   - **Status Calculation**:
     - `occupied_count == 0` $\rightarrow$ **Available** (Green)
     - `0 < occupied_count < capacity` $\rightarrow$ **Partially Occupied** (Amber)
     - `occupied_count >= capacity` $\rightarrow$ **Full** (Red)
   - **Deletion Protection (FR-4.3)**: When admin clicks Delete, `RoomDAO.deleteRoom()` checks if `ALLOCATION` has active records for that room. If active allocations exist, deletion is blocked with a warning dialog.
3. **Outcome**: Enforces room integrity and capacity boundaries.

---

### Step 5: Automated First-Fit Room Allocation & Multi-Student Sharing Flow

1. **User Action**: Admin selects a student ID and clicks **"Trigger First-Fit Allocation"**.
2. **Technical Process (`AllocationService.allocateStudent(studentId)`)**:
   - **Validation 1**: Verifies student exists in `STUDENT` table.
   - **Validation 2 (FR-5.5)**: Verifies student does NOT already hold an active allocation (`status = 'ACTIVE'`).
   - **First-Fit Execution**: `AllocationService` retrieves all rooms and invokes `AllocationAlgorithm.findFirstAvailableRoom(rooms)`.
   - **Algorithm Logic**:
     ```java
     for (Room room : rooms) {
         if (room.getOccupiedCount() < room.getCapacity()) {
             return room; // First available room with free bed capacity found!
         }
     }
     return null; // All rooms are full
     ```
   - **If Room Found**:
     - Increments room occupancy: `UPDATE ROOM SET occupied_count = occupied_count + 1 WHERE room_id = ?`.
     - Creates `ALLOCATION` record (`student_id`, `room_id`, `allocation_date`, `status = 'ACTIVE'`).
     - Multiple students share the room until `occupied_count == capacity`.
     - Automatically updates all roommate co-occupant displays.
     - If student was on the waiting list, updates waiting entry status to `'ALLOCATED'`.
   - **If No Room Found (Hostel Full)**:
     - Automatically inserts student into `WAITING_LIST` in **FIFO order** (`status = 'WAITING'`).

---

### Step 6: FIFO Waiting Queue Management Flow

1. **User Action**: Displays students waiting for hostel placement.
2. **Technical Process**:
   - `WaitingListDAO.getWaitingList()` queries `WAITING_LIST` ordered by `request_date ASC`.
   - Shows queue position number (#1, #2, #3...), student details, and request timestamp.
   - **Real-Time Queue Updating**: Whenever a student is added to the waiting queue or allocated, all metrics and queue tables refresh instantly across the UI.
   - Clicking **"Allocate Next FIFO Candidate"** calls `AllocationService.processNextWaitingStudent()`, allocating candidate #1 to the next available bed.

---

### Step 7: Vacating a Room & Automated Queue Re-allocation Flow

1. **User Action**: Admin selects an active allocation and clicks **"Vacate Selected Allocation"**.
2. **Technical Process (`AllocationService.vacateAllocation(allocationId)`)**:
   - Updates `ALLOCATION` record: `status = 'VACATED'` and sets `vacating_date = CURRENT_TIMESTAMP`.
   - Decrements room occupancy: `UPDATE ROOM SET occupied_count = GREATEST(0, occupied_count - 1) WHERE room_id = ?`.
   - **Automated Multi-Candidate Queue Fulfillment (FR-6.3)**: `AllocationService.processWaitingQueueUntilFull()` continuously checks if `WAITING_LIST` has pending students (`status = 'WAITING'`).
   - Automatically allocates pending waiting list candidates to freed beds until all rooms are full or the waiting queue is empty!

---

### Step 8: Reports & Audit Summaries Flow

1. **User Action**: Admin selects a report type (Active Allocations & Roommate Roster, Room Capacity & Roommate Breakdown, Waiting List).
2. **Technical Process**:
   - `ReportsController` fetches dataset from DAOs.
   - Under **Room-wise Capacity & Roommate Breakdown**, lists every single room along with its bed occupancy ratio (`occupied/capacity`) and the full roster of assigned roommate co-occupants.
3. **Outcome**: Displays detailed report output ready for viewing or printing.

---

## 📋 Summary Table of Core System Components

| Component / File | Class Path | Responsibilities |
|---|---|---|
| **App.java** | `com.hrams.App` | Primary JavaFX stage manager & scene switcher |
| **AllocationAlgorithm.java** | `com.hrams.algorithm.AllocationAlgorithm` | $O(n)$ First-Fit room lookup logic |
| **AllocationService.java** | `com.hrams.service.AllocationService` | Orchestrates allocation, multi-candidate queue fulfillment, & vacate workflows |
| **DatabaseConnection.java** | `com.hrams.util.DatabaseConnection` | JDBC connection manager with MySQL & Embedded H2 failover |
| **LoginController.java** | `com.hrams.controller.LoginController` | Asynchronous Admin login authentication |
| **DashboardController.java** | `com.hrams.controller.DashboardController` | Real-time KPI stats & roommate co-occupant table overview |
| **StudentController.java** | `com.hrams.controller.StudentController` | Student CRUD & search filter |
| **RoomController.java** | `com.hrams.controller.RoomController` | Room CRUD, capacity badges, & deletion protection |
| **AllocationController.java** | `com.hrams.controller.AllocationController` | First-Fit allocation trigger, roommate sharing display, & vacate management |
| **WaitingListController.java** | `com.hrams.controller.WaitingListController` | FIFO queue display & continuous queue candidate processing |
| **ReportsController.java** | `com.hrams.controller.ReportsController` | Roommate roster and occupancy audit reports generation |
| **styles.css** | `src/main/resources/css/styles.css` | High-contrast dark visual theme & table border grid styles |
