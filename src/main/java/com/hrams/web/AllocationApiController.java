package com.hrams.web;

import com.hrams.dao.AllocationDAO;
import com.hrams.model.Allocation;
import com.hrams.service.AllocationResult;
import com.hrams.service.AllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/allocations")
@CrossOrigin(origins = "*")
public class AllocationApiController {

    private final AllocationService allocationService = new AllocationService();
    private final AllocationDAO allocationDAO = new AllocationDAO();

    @GetMapping("/active")
    public ResponseEntity<List<Allocation>> getActiveAllocations() {
        try {
            return ResponseEntity.ok(allocationDAO.getAllActiveAllocations());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerAllocation(@RequestBody Map<String, String> req) {
        Map<String, Object> response = new HashMap<>();
        String studentId = req.get("studentId");

        if (studentId == null || studentId.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Student ID is required.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            AllocationResult result = allocationService.allocateStudent(studentId.trim());
            response.put("success", result.isSuccess());
            response.put("queuedToWaitingList", result.isQueuedToWaitingList());
            response.put("message", result.getMessage());

            if (result.getAllocation() != null) {
                response.put("allocation", result.getAllocation());
            }
            if (result.getAllocatedRoom() != null) {
                response.put("room", result.getAllocatedRoom());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Allocation Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/vacate/{id}")
    public ResponseEntity<Map<String, Object>> vacateAllocation(@PathVariable("id") int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean vacated = allocationService.vacateAllocation(id);
            response.put("success", vacated);
            response.put("message", vacated ? "Allocation vacated successfully. Waiting queue candidates processed automatically." : "Could not vacate allocation.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Vacate Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
