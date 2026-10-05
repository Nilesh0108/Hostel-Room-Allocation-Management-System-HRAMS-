# Product Requirements Document (PRD) & Software Requirements Specification (SRS)
## Hostel Room Allocation Management System (HRAMS)

---

### Part 1: Product Requirements Document (PRD)

#### 1. Project Title
**Hostel Room Allocation Management System (HRAMS)**

#### 2. Purpose
The purpose of this system is to replace manual, paper-based hostel room allocation with a digital system that lets an administrator manage students, rooms, allocations, and waiting lists in one place. The system should reduce errors like double-allocating a room or losing track of vacancies.

#### 3. Problem Statement
Manually tracking which student is in which room, how many beds are free, and who is waiting for a room becomes error-prone and slow as the number of students grows. There is no single source of truth for occupancy status, and re-allocating a room after a student vacates is difficult to track by hand.

#### 4. Goals and Objectives
- Give hostel administrators a single dashboard to view occupancy at a glance.
- Automate the assignment of students to available rooms based on capacity.
- Maintain a waiting list for students when no room is available.
- Keep an auditable record of allocations and vacancies.
- Provide a foundation that can later scale to more complex allocation rules (e.g., preference-based, gender-based, floor-based).

#### 5. Scope
**In scope:**
- Admin login
- Student record management (add/update/delete/search)
- Room record management (add/update/delete)
- Room allocation using a first-fit algorithm
- Waiting list management (FIFO queue)
- Basic dashboard statistics and reports
- Persistent storage in a relational database (MySQL)

**Out of scope (for this version):**
- Student self-service portal (students cannot log in themselves)
- Online payments or fee management
- Mobile application
- Multi-hostel or multi-campus support
- Notifications (SMS/email)

#### 6. User Roles
- **Administrator** — the only user role. Can log in, manage students and rooms, trigger allocations, view the waiting list, and view reports.

#### 7. Key Features
| Feature | Description |
|---|---|
| **Login** | Simple username/password authentication for the admin |
| **Dashboard** | Shows total students, available rooms, occupied rooms, and waiting students |
| **Student Management** | CRUD operations on student records |
| **Room Management** | CRUD operations on room records, including capacity and occupancy |
| **Room Allocation** | Assigns a student to the first available room with free capacity |
| **Waiting List** | Automatically queues students when no room is available, in FIFO order |
| **Reports** | Basic listing/summary of allocations and occupancy |

#### 8. Success Criteria
- A student can be allocated to a room in a single action without manual seat-counting.
- Room occupancy numbers on the dashboard always match the underlying allocation records.
- No room can ever be allocated beyond its stated capacity.
- A student who cannot be placed is never lost — they always appear on the waiting list.

#### 9. Assumptions
- Only one administrator uses the system at a time (no concurrent multi-admin editing conflict handling required).
- Each student can hold only one active allocation at a time.
- Room capacity and type do not change frequently once set.

#### 10. Constraints
- Desktop-based application only (JavaFX).
- Backend database is MySQL, accessed via JDBC.
- No internet/cloud dependency required.

---

### Part 2: Software Requirements Specification (SRS)

#### 1. Introduction
##### 1.1 Purpose
This document specifies the functional and non-functional requirements for the Hostel Room Allocation Management System (HRAMS), a desktop application for managing student hostel room allocations.

##### 1.2 Intended Audience
Developers, evaluators/reviewers of the project, and the project team itself.

##### 1.3 Scope
HRAMS allows an administrator to manage student and room data, allocate rooms automatically, maintain a waiting list, and view basic reports and dashboard statistics.

##### 1.4 Definitions and Abbreviations
| Term | Meaning |
|---|---|
| **DAO** | Data Access Object — handles database read/write operations |
| **CRUD** | Create, Read, Update, Delete |
| **FIFO** | First-In-First-Out (queue ordering) |
| **JDBC** | Java Database Connectivity |
| **FXML** | XML-based UI markup used by JavaFX |

#### 2. Overall Description
##### 2.1 Product Perspective
HRAMS is a standalone desktop application built with JavaFX for the front end and MySQL for persistent storage, connected through JDBC. It is not part of a larger product family and has no external system integrations.

##### 2.2 Product Functions
- Authenticate the administrator
- Manage student records
- Manage room records
- Allocate students to rooms based on availability
- Maintain a waiting list for unallocated students
- Display dashboard statistics
- Generate basic reports

##### 2.3 User Characteristics
A single type of user — the administrator — is assumed to have basic computer literacy but no technical/database expertise. The interface should therefore be simple, form-based, and require no direct database interaction.

##### 2.4 Constraints
- Must be built using JavaFX and MySQL as specified by the project requirements.
- Single-admin usage; no multi-user concurrency handling required.
- No external network access needed.

##### 2.5 Assumptions and Dependencies
- Java runtime and MySQL server are available on the machine running the application.
- The database schema is created before the application is run.

#### 3. Functional Requirements
- **FR-1: Login**
  - FR-1.1: The system shall provide a login screen requiring a username and password.
  - FR-1.2: The system shall validate credentials against the ADMIN table.
  - FR-1.3: On successful login, the system shall navigate to the Dashboard.
  - FR-1.4: On failed login, the system shall display an error message.
- **FR-2: Dashboard**
  - FR-2.1: The system shall display total number of students.
  - FR-2.2: The system shall display total available rooms.
  - FR-2.3: The system shall display total occupied rooms.
  - FR-2.4: The system shall display total students on the waiting list.
- **FR-3: Student Management**
  - FR-3.1: The system shall allow adding a new student record (ID, name, email, phone, course, year, gender).
  - FR-3.2: The system shall allow updating an existing student record.
  - FR-3.3: The system shall allow deleting a student record.
  - FR-3.4: The system shall allow searching for a student by ID or name.
  - FR-3.5: The system shall display all students in a tabular view.
- **FR-4: Room Management**
  - FR-4.1: The system shall allow adding a new room (room number, capacity, room type, floor).
  - FR-4.2: The system shall allow updating room details.
  - FR-4.3: The system shall allow deleting a room, provided it has no active allocations.
  - FR-4.4: The system shall display each room's current occupancy and status (Available/Full).
- **FR-5: Room Allocation**
  - FR-5.1: The system shall allow the admin to initiate allocation for a given student ID.
  - FR-5.2: The system shall check rooms in order and allocate the first room where occupied < capacity.
  - FR-5.3: The system shall increment the room's occupancy count upon successful allocation.
  - FR-5.4: The system shall create an allocation record with student ID, room ID, and allocation date.
  - FR-5.5: The system shall prevent a student from holding more than one active allocation.
  - FR-5.6: If no room is available, the system shall add the student to the waiting list.
- **FR-6: Waiting List**
  - FR-6.1: The system shall maintain waiting-list entries in FIFO order.
  - FR-6.2: The system shall display all students currently on the waiting list.
  - FR-6.3: When a room becomes available (e.g., a student vacates), the system shall allow allocating it to the next student in the waiting list.
- **FR-7: Vacating a Room**
  - FR-7.1: The system shall allow the admin to mark an allocation as vacated.
  - FR-7.2: On vacating, the system shall decrement the room's occupancy count and update the allocation's vacating date and status.
- **FR-8: Reports**
  - FR-8.1: The system shall generate a report of current allocations.
  - FR-8.2: The system shall generate a report of room-wise occupancy.
  - FR-8.3: The system shall generate a report of waiting-list students.

#### 4. Non-Functional Requirements
| ID | Requirement |
|---|---|
| **NFR-1** | **Usability**: The UI shall use simple forms and tables understandable without training. |
| **NFR-2** | **Performance**: Allocation lookup shall complete in O(n) time relative to the number of rooms, with negligible delay for typical hostel sizes (few hundred rooms). |
| **NFR-3** | **Reliability**: The system shall not allow room occupancy to exceed capacity under any operation. |
| **NFR-4** | **Maintainability**: The system shall follow a layered architecture (Controller → Service → Algorithm → DAO) to allow future changes without rewriting the UI. |
| **NFR-5** | **Portability**: The system shall run on any OS with a compatible JVM and MySQL installation. |
| **NFR-6** | **Data Integrity**: Foreign key constraints shall be enforced between STUDENT, ROOM, ALLOCATION, and WAITING_LIST tables. |

#### 5. External Interface Requirements
##### 5.1 User Interfaces
- Login screen
- Dashboard screen
- Student Management screen
- Room Management screen
- Room Allocation screen
- Waiting List screen
- Reports screen

##### 5.2 Hardware Interfaces
None beyond standard desktop/laptop hardware.

##### 5.3 Software Interfaces
- JavaFX (UI framework)
- MySQL (database)
- JDBC (database connectivity layer)

##### 5.4 Communication Interfaces
None (fully local, single-machine application).

#### 6. System Architecture Overview
`JavaFX UI → Controller → Service → Algorithm → DAO → JDBC → MySQL`

This layered design ensures the UI layer built early can later be connected to a working backend without redesign.

#### 7. Database Requirements (Entities)
| Table | Key Fields |
|---|---|
| **ADMIN** | `admin_id` (PK), `username`, `password` |
| **STUDENT** | `student_id` (PK), `name`, `email`, `phone`, `course`, `year`, `gender` |
| **ROOM** | `room_id` (PK), `room_number` (unique), `capacity`, `room_type`, `floor` |
| **ALLOCATION** | `allocation_id` (PK), `student_id` (FK), `room_id` (FK), `allocation_date`, `vacating_date`, `status` |
| **WAITING_LIST** | `waiting_id` (PK), `student_id` (FK), `request_date`, `status` |

*Relationship rule:* One student → many allocation records over time; each allocation → one room; a student may have at most one active allocation at a time.

#### 8. Other Requirements
- The system shall log key actions (allocation, vacating) implicitly via the ALLOCATION table's status/date fields for auditability.
- Exception handling shall be present for invalid inputs (e.g., allocating a non-existent student ID).
