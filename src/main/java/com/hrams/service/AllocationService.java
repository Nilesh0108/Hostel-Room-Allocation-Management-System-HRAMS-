package com.hrams.service;

import com.hrams.algorithm.AllocationAlgorithm;
import com.hrams.dao.AllocationDAO;
import com.hrams.dao.RoomDAO;
import com.hrams.dao.StudentDAO;
import com.hrams.dao.WaitingListDAO;
import com.hrams.model.Allocation;
import com.hrams.model.Room;
import com.hrams.model.Student;
import com.hrams.model.WaitingEntry;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class AllocationService {
    private final StudentDAO studentDAO;
    private final RoomDAO roomDAO;
    private final AllocationDAO allocationDAO;
    private final WaitingListDAO waitingListDAO;
    private final AllocationAlgorithm algorithm;

    public AllocationService() {
        this.studentDAO = new StudentDAO();
        this.roomDAO = new RoomDAO();
        this.allocationDAO = new AllocationDAO();
        this.waitingListDAO = new WaitingListDAO();
        this.algorithm = new AllocationAlgorithm();
    }

    public AllocationService(StudentDAO studentDAO, RoomDAO roomDAO, AllocationDAO allocationDAO, WaitingListDAO waitingListDAO, AllocationAlgorithm algorithm) {
        this.studentDAO = studentDAO;
        this.roomDAO = roomDAO;
        this.allocationDAO = allocationDAO;
        this.waitingListDAO = waitingListDAO;
        this.algorithm = algorithm;
    }

    /**
     * FR-5: Core Allocation Workflow
     */
    public AllocationResult allocateStudent(String studentId) throws SQLException {
        Student student = studentDAO.getStudentById(studentId);
        if (student == null) {
            return AllocationResult.failed("Student ID '" + studentId + "' does not exist!");
        }

        Allocation active = allocationDAO.getActiveAllocationByStudent(studentId);
        if (active != null) {
            return AllocationResult.failed("Student " + student.getName() + " (" + studentId + ") already holds active allocation in Room " + active.getRoomNumber() + "!");
        }

        List<Room> rooms = roomDAO.getAllRooms();
        Room selectedRoom = algorithm.findFirstAvailableRoom(rooms);

        if (selectedRoom != null) {
            boolean incremented = roomDAO.incrementOccupancy(selectedRoom.getRoomId());
            if (!incremented) {
                rooms = roomDAO.getAllRooms();
                selectedRoom = algorithm.findFirstAvailableRoom(rooms);
                if (selectedRoom == null) {
                    return queueToWaitingList(student);
                }
                roomDAO.incrementOccupancy(selectedRoom.getRoomId());
            }

            Allocation allocation = allocationDAO.createAllocation(studentId, selectedRoom.getRoomId());

            if (waitingListDAO.isStudentOnWaitingList(studentId)) {
                List<WaitingEntry> waitingEntries = waitingListDAO.getWaitingList();
                for (WaitingEntry entry : waitingEntries) {
                    if (entry.getStudentId().equalsIgnoreCase(studentId)) {
                        waitingListDAO.updateStatus(entry.getWaitingId(), "ALLOCATED");
                    }
                }
            }

            Room updatedRoom = roomDAO.getRoomById(selectedRoom.getRoomId());
            return AllocationResult.allocated(
                allocation,
                updatedRoom,
                "Successfully allocated " + student.getName() + " to Room " + updatedRoom.getRoomNumber() + " (" + updatedRoom.getOccupiedCount() + "/" + updatedRoom.getCapacity() + " Beds Occupied)."
            );
        } else {
            return queueToWaitingList(student);
        }
    }

    private AllocationResult queueToWaitingList(Student student) throws SQLException {
        if (waitingListDAO.isStudentOnWaitingList(student.getStudentId())) {
            return AllocationResult.failed("No rooms available. Student " + student.getName() + " is ALREADY queued on the waiting list.");
        }

        boolean queued = waitingListDAO.addToWaitingList(student.getStudentId());
        if (queued) {
            return AllocationResult.queued("No rooms available. Student " + student.getName() + " (" + student.getStudentId() + ") has been added to the WAITING LIST in FIFO order.");
        } else {
            return AllocationResult.failed("Could not add student to waiting list!");
        }
    }

    /**
     * FR-7: Vacate allocation workflow
     */
    public boolean vacateAllocation(int allocationId) throws SQLException {
        Allocation allocation = allocationDAO.getAllocationById(allocationId);
        if (allocation == null || !"ACTIVE".equalsIgnoreCase(allocation.getStatus())) {
            return false;
        }

        boolean vacated = allocationDAO.vacateAllocation(allocationId);
        if (vacated) {
            roomDAO.decrementOccupancy(allocation.getRoomId());
            // Automatically allocate waiting list members to available room beds
            processWaitingQueueUntilFull();
            return true;
        }
        return false;
    }

    /**
     * FR-6.3 & Queue Auto-fulfillment: Process waiting queue members continuously until all rooms are full or queue is empty
     */
    public int processWaitingQueueUntilFull() throws SQLException {
        int count = 0;
        while (true) {
            WaitingEntry next = waitingListDAO.getNextWaitingStudent();
            if (next == null) break; // Queue empty

            List<Room> rooms = roomDAO.getAllRooms();
            Room room = algorithm.findFirstAvailableRoom(rooms);
            if (room == null) break; // All rooms full

            boolean incremented = roomDAO.incrementOccupancy(room.getRoomId());
            if (incremented) {
                allocationDAO.createAllocation(next.getStudentId(), room.getRoomId());
                waitingListDAO.updateStatus(next.getWaitingId(), "ALLOCATED");
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    public AllocationResult processNextWaitingStudent() throws SQLException {
        WaitingEntry next = waitingListDAO.getNextWaitingStudent();
        if (next == null) {
            return AllocationResult.failed("Waiting list is empty.");
        }

        List<Room> rooms = roomDAO.getAllRooms();
        Room room = algorithm.findFirstAvailableRoom(rooms);
        if (room == null) {
            return AllocationResult.failed("No rooms available for waiting student.");
        }

        boolean incremented = roomDAO.incrementOccupancy(room.getRoomId());
        if (incremented) {
            Allocation allocation = allocationDAO.createAllocation(next.getStudentId(), room.getRoomId());
            waitingListDAO.updateStatus(next.getWaitingId(), "ALLOCATED");
            Room updatedRoom = roomDAO.getRoomById(room.getRoomId());
            return AllocationResult.allocated(
                allocation,
                updatedRoom,
                "Waiting student " + next.getStudentName() + " (" + next.getStudentId() + ") automatically allocated to Room " + updatedRoom.getRoomNumber() + " (" + updatedRoom.getOccupiedCount() + "/" + updatedRoom.getCapacity() + " Beds)!"
            );
        }
        return AllocationResult.failed("Failed to allocate room to waiting student.");
    }
}
