package com.hrams.web;

import com.hrams.dao.RoomDAO;
import com.hrams.model.Room;
import com.hrams.service.AllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomApiController {

    private final RoomDAO roomDAO = new RoomDAO();
    private final AllocationService allocationService = new AllocationService();

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        try {
            return ResponseEntity.ok(roomDAO.getAllRooms());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addRoom(@RequestBody Room room) {
        Map<String, Object> res = new HashMap<>();
        try {
            if (room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty()) {
                res.put("success", false);
                res.put("message", "Room Number is required.");
                return ResponseEntity.badRequest().body(res);
            }
            if (roomDAO.getRoomByNumber(room.getRoomNumber().trim()) != null) {
                res.put("success", false);
                res.put("message", "Room number '" + room.getRoomNumber() + "' already exists!");
                return ResponseEntity.badRequest().body(res);
            }
            boolean added = roomDAO.addRoom(room);
            if (added) {
                // If capacity is added, auto-fulfill any waiting list members
                allocationService.processWaitingQueueUntilFull();
            }
            res.put("success", added);
            res.put("message", added ? "Room record added successfully." : "Could not add room.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Database error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRoom(@PathVariable("id") int id, @RequestBody Room room) {
        Map<String, Object> res = new HashMap<>();
        try {
            room.setRoomId(id);
            boolean updated = roomDAO.updateRoom(room);
            if (updated) {
                allocationService.processWaitingQueueUntilFull();
            }
            res.put("success", updated);
            res.put("message", updated ? "Room updated successfully." : "Room not found.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Database error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRoom(@PathVariable("id") int id) {
        Map<String, Object> res = new HashMap<>();
        try {
            boolean deleted = roomDAO.deleteRoom(id);
            res.put("success", deleted);
            res.put("message", deleted ? "Room deleted successfully." : "Room not found.");
            return ResponseEntity.ok(res);
        } catch (IllegalStateException e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Delete error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }
}
