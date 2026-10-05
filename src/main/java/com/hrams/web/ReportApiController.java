package com.hrams.web;

import com.hrams.dao.AllocationDAO;
import com.hrams.dao.RoomDAO;
import com.hrams.dao.WaitingListDAO;
import com.hrams.model.Allocation;
import com.hrams.model.Room;
import com.hrams.model.WaitingEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportApiController {

    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final WaitingListDAO waitingListDAO = new WaitingListDAO();

    @GetMapping("/{type}")
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable("type") int type) {
        Map<String, Object> response = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try {
            switch (type) {
                case 1:
                    generateAllocationsReport(sb, timestamp);
                    response.put("title", "Report: Active Hostel Room Allocations & Roommates");
                    break;
                case 2:
                    generateOccupancyReport(sb, timestamp);
                    response.put("title", "Report: Room Capacity & Assigned Roommate Roster");
                    break;
                case 3:
                    generateWaitingListReport(sb, timestamp);
                    response.put("title", "Report: FIFO Waiting List Status");
                    break;
                default:
                    response.put("error", "Invalid report type.");
                    return ResponseEntity.badRequest().body(response);
            }
            response.put("success", true);
            response.put("content", sb.toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Report Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private void generateAllocationsReport(StringBuilder sb, String timestamp) throws Exception {
        List<Allocation> list = allocationDAO.getAllActiveAllocations();
        sb.append("====================================================================================================\n");
        sb.append("             HOSTEL ROOM ALLOCATION SYSTEM - ACTIVE ALLOCATIONS & ROOMMATE ROSTER                  \n");
        sb.append("Generated At: ").append(timestamp).append("\n");
        sb.append("Total Active Allocations: ").append(list.size()).append("\n");
        sb.append("====================================================================================================\n\n");
        sb.append(String.format("%-9s | %-12s | %-20s | %-16s | %-26s | %-8s\n", "Alloc ID", "Student ID", "Student Name", "Room & Capacity", "Co-Occupants / Roommates", "Status"));
        sb.append("----------------------------------------------------------------------------------------------------\n");

        for (Allocation a : list) {
            sb.append(String.format("%-9d | %-12s | %-20s | %-16s | %-26s | %-8s\n",
                a.getAllocationId(),
                a.getStudentId(),
                truncate(a.getStudentName(), 20),
                a.getRoomCapacityDisplay(),
                truncate(a.getCoOccupants(), 26),
                a.getStatus()
            ));
        }
        sb.append("----------------------------------------------------------------------------------------------------\n");
        sb.append("END OF REPORT\n");
    }

    private void generateOccupancyReport(StringBuilder sb, String timestamp) throws Exception {
        List<Room> rooms = roomDAO.getAllRooms();
        int totalCapacity = 0;
        int totalOccupied = 0;

        for (Room r : rooms) {
            totalCapacity += r.getCapacity();
            totalOccupied += r.getOccupiedCount();
        }

        sb.append("====================================================================================================\n");
        sb.append("          HOSTEL ROOM ALLOCATION SYSTEM - ROOM CAPACITY & ROOMMATE BREAKDOWN REPORT                 \n");
        sb.append("Generated At: ").append(timestamp).append("\n");
        sb.append("Total Rooms: ").append(rooms.size()).append(" | Total Beds: ").append(totalCapacity).append(" | Occupied Beds: ").append(totalOccupied).append(" | Vacant Beds: ").append(totalCapacity - totalOccupied).append("\n");
        sb.append("====================================================================================================\n\n");

        for (Room r : rooms) {
            List<Allocation> allocs = allocationDAO.getAllocationsByRoomId(r.getRoomId());
            sb.append("🏢 Room ").append(r.getRoomNumber()).append(" (Floor ").append(r.getFloor()).append(", Type: ").append(r.getRoomType()).append(")\n");
            sb.append("   Bed Occupancy : ").append(r.getOccupiedCount()).append(" of ").append(r.getCapacity()).append(" beds occupied (Status: ").append(r.getStatus()).append(")\n");
            sb.append("   Assigned Roommates:\n");
            if (allocs.isEmpty()) {
                sb.append("     - [No students currently allocated to this room]\n");
            } else {
                for (Allocation a : allocs) {
                    sb.append("     - ").append(a.getStudentName()).append(" (ID: ").append(a.getStudentId()).append(", Allocated: ").append(a.getAllocationDate() != null ? a.getAllocationDate().toString() : "").append(")\n");
                }
            }
            sb.append("----------------------------------------------------------------------------------------------------\n");
        }
        sb.append("END OF REPORT\n");
    }

    private void generateWaitingListReport(StringBuilder sb, String timestamp) throws Exception {
        List<WaitingEntry> list = waitingListDAO.getWaitingList();
        sb.append("====================================================================================================\n");
        sb.append("                HOSTEL ROOM ALLOCATION SYSTEM - WAITING LIST REPORT                                 \n");
        sb.append("Generated At: ").append(timestamp).append("\n");
        sb.append("Total Pending Students in Queue: ").append(list.size()).append("\n");
        sb.append("====================================================================================================\n\n");
        sb.append(String.format("%-8s | %-10s | %-12s | %-22s | %-18s | %-20s\n", "Queue #", "Wait ID", "Student ID", "Student Name", "Course", "Request Date"));
        sb.append("----------------------------------------------------------------------------------------------------\n");

        int pos = 1;
        for (WaitingEntry w : list) {
            sb.append(String.format("%-8d | %-10d | %-12s | %-22s | %-18s | %-20s\n",
                pos++,
                w.getWaitingId(),
                w.getStudentId(),
                truncate(w.getStudentName(), 22),
                truncate(w.getStudentCourse(), 18),
                w.getRequestDate() != null ? w.getRequestDate().toString() : ""
            ));
        }
        sb.append("----------------------------------------------------------------------------------------------------\n");
        sb.append("END OF REPORT\n");
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
