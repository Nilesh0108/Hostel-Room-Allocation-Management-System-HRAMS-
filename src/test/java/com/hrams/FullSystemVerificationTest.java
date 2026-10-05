package com.hrams;

import com.hrams.algorithm.AllocationAlgorithm;
import com.hrams.dao.*;
import com.hrams.model.*;
import com.hrams.service.AllocationResult;
import com.hrams.service.AllocationService;
import com.hrams.util.DatabaseConnection;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FullSystemVerificationTest {

    private static AdminDAO adminDAO;
    private static StudentDAO studentDAO;
    private static RoomDAO roomDAO;
    private static AllocationDAO allocationDAO;
    private static WaitingListDAO waitingListDAO;
    private static AllocationService allocationService;
    private static AllocationAlgorithm algorithm;

    @BeforeAll
    public static void setUpClass() throws Exception {
        adminDAO = new AdminDAO();
        studentDAO = new StudentDAO();
        roomDAO = new RoomDAO();
        allocationDAO = new AllocationDAO();
        waitingListDAO = new WaitingListDAO();
        allocationService = new AllocationService();
        algorithm = new AllocationAlgorithm();

        // Initialize DB connection and clear old data for clean test run
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM ALLOCATION");
            stmt.executeUpdate("DELETE FROM WAITING_LIST");
            stmt.executeUpdate("DELETE FROM STUDENT");
            stmt.executeUpdate("DELETE FROM ROOM");
        }
    }

    @Test
    @Order(1)
    @DisplayName("FR-1: Admin Authentication Test")
    public void testFR1_AdminAuthentication() throws Exception {
        // Valid Admin
        Admin admin = adminDAO.authenticate("admin", "admin123");
        assertNotNull(admin, "Admin authentication should succeed with default credentials admin/admin123");
        assertEquals("admin", admin.getUsername());

        // Invalid Admin
        Admin invalid = adminDAO.authenticate("admin", "wrongpassword");
        assertNull(invalid, "Admin authentication should fail for invalid password");
    }

    @Test
    @Order(2)
    @DisplayName("FR-3: Student Management CRUD & Search Test")
    public void testFR3_StudentManagement() throws Exception {
        // Add Students
        Student s1 = new Student("STU-TEST-1", "Alice Wonder", "alice@univ.edu", "+1-555-1001", "Computer Science", 1,
                "Female");
        Student s2 = new Student("STU-TEST-2", "Bob Builder", "bob@univ.edu", "+1-555-1002", "Civil Engineering", 2,
                "Male");
        Student s3 = new Student("STU-TEST-3", "Charlie Brown", "charlie@univ.edu", "+1-555-1003", "Arts", 3, "Male");

        assertTrue(studentDAO.addStudent(s1));
        assertTrue(studentDAO.addStudent(s2));
        assertTrue(studentDAO.addStudent(s3));

        assertEquals(3, studentDAO.getTotalStudentCount(), "Total students count should be 3");

        // Search Student
        List<Student> searchResults = studentDAO.searchStudents("Alice");
        assertEquals(1, searchResults.size());
        assertEquals("STU-TEST-1", searchResults.get(0).getStudentId());

        // Update Student
        s1.setCourse("Data Science");
        assertTrue(studentDAO.updateStudent(s1));
        Student updated = studentDAO.getStudentById("STU-TEST-1");
        assertEquals("Data Science", updated.getCourse());
    }

    @Test
    @Order(3)
    @DisplayName("FR-4: Room Management CRUD & Capacity Status Test")
    public void testFR4_RoomManagement() throws Exception {
        // Add Rooms
        Room r1 = new Room("R-101", 1, "Single Standard", 1); // Capacity 1
        Room r2 = new Room("R-102", 2, "Double Deluxe", 1); // Capacity 2

        assertTrue(roomDAO.addRoom(r1));
        assertTrue(roomDAO.addRoom(r2));

        List<Room> rooms = roomDAO.getAllRooms();
        assertEquals(2, rooms.size());

        // Check Status
        assertEquals("Available", r1.getStatus());
        assertEquals(1, r1.getAvailableBeds());

        // Update Room
        r2.setCapacity(3);
        assertTrue(roomDAO.updateRoom(r2));
        Room updatedR2 = roomDAO.getRoomById(r2.getRoomId());
        assertEquals(3, updatedR2.getCapacity());
    }

    @Test
    @Order(4)
    @DisplayName("FR-5: First-Fit Room Allocation & Boundary Enforcement")
    public void testFR5_FirstFitAllocation() throws Exception {
        // Allocate Student 1 -> Should be placed in R-101 (First available room)
        AllocationResult res1 = allocationService.allocateStudent("STU-TEST-1");
        assertTrue(res1.isSuccess());
        assertFalse(res1.isQueuedToWaitingList());
        assertEquals("R-101", res1.getAllocatedRoom().getRoomNumber());

        // Verify R-101 is now full (Cap 1, Occ 1)
        Room r1 = roomDAO.getRoomByNumber("R-101");
        assertEquals(1, r1.getOccupiedCount());
        assertEquals("Full", r1.getStatus());

        // FR-5.5: Prevent Duplicate Active Allocation for Student 1
        AllocationResult dupRes = allocationService.allocateStudent("STU-TEST-1");
        assertFalse(dupRes.isSuccess(), "Should reject student attempting second active allocation");
        assertTrue(dupRes.getMessage().contains("already holds active allocation"));

        // Allocate Student 2 -> R-101 is full, should pick next available room R-102
        // (Cap 3, Occ 0 -> 1)
        AllocationResult res2 = allocationService.allocateStudent("STU-TEST-2");
        assertTrue(res2.isSuccess());
        assertEquals("R-102", res2.getAllocatedRoom().getRoomNumber());
    }

    @Test
    @Order(5)
    @DisplayName("FR-5.6 & FR-6: FIFO Waiting List Queuing when Hostel is Full")
    public void testFR6_WaitingListQueuing() throws Exception {
        // Fill remaining capacity of R-102 (Cap 3, Occ 1 -> fill up to 3)
        Room r2 = roomDAO.getRoomByNumber("R-102");
        roomDAO.incrementOccupancy(r2.getRoomId()); // Occ 2
        roomDAO.incrementOccupancy(r2.getRoomId()); // Occ 3 (FULL)

        // Add 4th student
        Student s4 = new Student("STU-TEST-4", "Diana Prince", "diana@univ.edu", "+1-555-1004", "Law", 4, "Female");
        studentDAO.addStudent(s4);

        // Attempt allocation for Student 4 when all rooms are full
        AllocationResult res4 = allocationService.allocateStudent("STU-TEST-4");
        assertTrue(res4.isSuccess(), "Result should succeed in queuing student");
        assertTrue(res4.isQueuedToWaitingList(), "Student should be queued to waiting list");
        assertEquals(1, waitingListDAO.getWaitingCount(), "Waiting list count should be 1");

        // Add 5th student and queue
        Student s5 = new Student("STU-TEST-5", "Evan Wright", "evan@univ.edu", "+1-555-1005", "Physics", 2, "Male");
        studentDAO.addStudent(s5);
        AllocationResult res5 = allocationService.allocateStudent("STU-TEST-5");
        assertTrue(res5.isQueuedToWaitingList());
        assertEquals(2, waitingListDAO.getWaitingCount());

        // Verify FIFO order: STU-TEST-4 should be head of queue
        WaitingEntry nextInQueue = waitingListDAO.getNextWaitingStudent();
        assertEquals("STU-TEST-4", nextInQueue.getStudentId());
    }

    @Test
    @Order(6)
    @DisplayName("FR-7 & FR-6.3: Room Vacating & Automated Reallocation to Waiting Candidate")
    public void testFR7_VacateAndReallocate() throws Exception {
        // Get active allocation for Student 1 in R-101
        Allocation alloc1 = allocationDAO.getActiveAllocationByStudent("STU-TEST-1");
        assertNotNull(alloc1);

        // Vacate allocation for Student 1
        boolean vacated = allocationService.vacateAllocation(alloc1.getAllocationId());
        assertTrue(vacated, "Allocation should be vacated successfully");

        // R-101 becomes free -> AllocationService automatically allocated next FIFO
        // candidate (STU-TEST-4) to R-101!
        Allocation alloc4 = allocationDAO.getActiveAllocationByStudent("STU-TEST-4");
        assertNotNull(alloc4, "STU-TEST-4 should be automatically allocated to vacated room");
        assertEquals("R-101", alloc4.getRoomNumber());

        // Waiting queue count should decrease from 2 to 1
        assertEquals(1, waitingListDAO.getWaitingCount());
    }

    @Test
    @Order(7)
    @DisplayName("FR-2 & FR-8: Dashboard Metrics & Reports Verification")
    public void testFR8_DashboardAndReports() throws Exception {
        int totalStudents = studentDAO.getTotalStudentCount();
        assertEquals(5, totalStudents, "Total registered students should be 5");

        List<Allocation> activeAllocations = allocationDAO.getAllActiveAllocations();
        assertFalse(activeAllocations.isEmpty(), "Active allocations report should return list of active records");

        List<WaitingEntry> waitingList = waitingListDAO.getWaitingList();
        assertEquals(1, waitingList.size(), "Waiting list report should contain 1 pending candidate (STU-TEST-5)");
    }
}
