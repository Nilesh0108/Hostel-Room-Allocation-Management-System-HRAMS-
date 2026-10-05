package com.hrams.web;

import com.hrams.dao.AllocationDAO;
import com.hrams.dao.RoomDAO;
import com.hrams.dao.StudentDAO;
import com.hrams.dao.WaitingListDAO;
import com.hrams.model.Allocation;
import com.hrams.util.DatabaseConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardApiController {

    private final StudentDAO studentDAO = new StudentDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final WaitingListDAO waitingListDAO = new WaitingListDAO();
    private final AllocationDAO allocationDAO = new AllocationDAO();

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            int totalStudents = studentDAO.getTotalStudentCount();
            int availableRooms = roomDAO.getAvailableRoomCount();
            int occupiedRooms = roomDAO.getOccupiedRoomCount();
            int waitingCount = waitingListDAO.getWaitingCount();
            List<Allocation> activeAllocations = allocationDAO.getAllActiveAllocations();

            response.put("totalStudents", totalStudents);
            response.put("availableRooms", availableRooms);
            response.put("occupiedRooms", occupiedRooms);
            response.put("waitingStudents", waitingCount);
            response.put("activeAllocations", activeAllocations);
            response.put("databaseType", DatabaseConnection.getActiveDatabaseType());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
