package com.hrams.web;

import com.hrams.dao.WaitingListDAO;
import com.hrams.model.WaitingEntry;
import com.hrams.service.AllocationResult;
import com.hrams.service.AllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/waiting-list")
@CrossOrigin(origins = "*")
public class WaitingListApiController {

    private final WaitingListDAO waitingListDAO = new WaitingListDAO();
    private final AllocationService allocationService = new AllocationService();

    @GetMapping
    public ResponseEntity<List<WaitingEntry>> getWaitingList() {
        try {
            return ResponseEntity.ok(waitingListDAO.getWaitingList());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/process-next")
    public ResponseEntity<Map<String, Object>> processNextCandidate() {
        Map<String, Object> response = new HashMap<>();
        try {
            AllocationResult result = allocationService.processNextWaitingStudent();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
