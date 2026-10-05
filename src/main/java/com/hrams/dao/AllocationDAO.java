package com.hrams.dao;

import com.hrams.model.Allocation;
import com.hrams.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AllocationDAO {

    public Allocation createAllocation(String studentId, int roomId) throws SQLException {
        String sql = "INSERT INTO ALLOCATION (student_id, room_id, allocation_date, status) VALUES (?, ?, CURRENT_TIMESTAMP, 'ACTIVE')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, studentId);
            stmt.setInt(2, roomId);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        return getAllocationById(id);
                    }
                }
            }
        }
        return null;
    }

    public Allocation getActiveAllocationByStudent(String studentId) throws SQLException {
        String sql = "SELECT a.*, s.name as student_name, r.room_number, r.capacity as room_capacity, r.occupied_count as room_occupied, " +
                     "(SELECT GROUP_CONCAT(s2.name SEPARATOR ', ') FROM ALLOCATION a2 JOIN STUDENT s2 ON a2.student_id = s2.student_id WHERE a2.room_id = a.room_id AND a2.status = 'ACTIVE') as co_occupants " +
                     "FROM ALLOCATION a " +
                     "JOIN STUDENT s ON a.student_id = s.student_id " +
                     "JOIN ROOM r ON a.room_id = r.room_id " +
                     "WHERE a.student_id = ? AND a.status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToAllocation(rs);
                }
            }
        }
        return null;
    }

    public Allocation getAllocationById(int allocationId) throws SQLException {
        String sql = "SELECT a.*, s.name as student_name, r.room_number, r.capacity as room_capacity, r.occupied_count as room_occupied, " +
                     "(SELECT GROUP_CONCAT(s2.name SEPARATOR ', ') FROM ALLOCATION a2 JOIN STUDENT s2 ON a2.student_id = s2.student_id WHERE a2.room_id = a.room_id AND a2.status = 'ACTIVE') as co_occupants " +
                     "FROM ALLOCATION a " +
                     "JOIN STUDENT s ON a.student_id = s.student_id " +
                     "JOIN ROOM r ON a.room_id = r.room_id " +
                     "WHERE a.allocation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, allocationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToAllocation(rs);
                }
            }
        }
        return null;
    }

    public List<Allocation> getAllActiveAllocations() throws SQLException {
        List<Allocation> allocations = new ArrayList<>();
        String sql = "SELECT a.*, s.name as student_name, r.room_number, r.capacity as room_capacity, r.occupied_count as room_occupied, " +
                     "(SELECT GROUP_CONCAT(s2.name SEPARATOR ', ') FROM ALLOCATION a2 JOIN STUDENT s2 ON a2.student_id = s2.student_id WHERE a2.room_id = a.room_id AND a2.status = 'ACTIVE') as co_occupants " +
                     "FROM ALLOCATION a " +
                     "JOIN STUDENT s ON a.student_id = s.student_id " +
                     "JOIN ROOM r ON a.room_id = r.room_id " +
                     "WHERE a.status = 'ACTIVE' ORDER BY r.room_number ASC, a.allocation_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                allocations.add(mapRowToAllocation(rs));
            }
        }
        return allocations;
    }

    public List<Allocation> getAllAllocations() throws SQLException {
        List<Allocation> allocations = new ArrayList<>();
        String sql = "SELECT a.*, s.name as student_name, r.room_number, r.capacity as room_capacity, r.occupied_count as room_occupied, " +
                     "(SELECT GROUP_CONCAT(s2.name SEPARATOR ', ') FROM ALLOCATION a2 JOIN STUDENT s2 ON a2.student_id = s2.student_id WHERE a2.room_id = a.room_id AND a2.status = 'ACTIVE') as co_occupants " +
                     "FROM ALLOCATION a " +
                     "JOIN STUDENT s ON a.student_id = s.student_id " +
                     "JOIN ROOM r ON a.room_id = r.room_id " +
                     "ORDER BY a.allocation_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                allocations.add(mapRowToAllocation(rs));
            }
        }
        return allocations;
    }

    public List<Allocation> getAllocationsByRoomId(int roomId) throws SQLException {
        List<Allocation> list = new ArrayList<>();
        String sql = "SELECT a.*, s.name as student_name, r.room_number, r.capacity as room_capacity, r.occupied_count as room_occupied, " +
                     "(SELECT GROUP_CONCAT(s2.name SEPARATOR ', ') FROM ALLOCATION a2 JOIN STUDENT s2 ON a2.student_id = s2.student_id WHERE a2.room_id = a.room_id AND a2.status = 'ACTIVE') as co_occupants " +
                     "FROM ALLOCATION a " +
                     "JOIN STUDENT s ON a.student_id = s.student_id " +
                     "JOIN ROOM r ON a.room_id = r.room_id " +
                     "WHERE a.room_id = ? AND a.status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToAllocation(rs));
                }
            }
        }
        return list;
    }

    public boolean vacateAllocation(int allocationId) throws SQLException {
        String sql = "UPDATE ALLOCATION SET status = 'VACATED', vacating_date = CURRENT_TIMESTAMP WHERE allocation_id = ? AND status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, allocationId);
            return stmt.executeUpdate() > 0;
        }
    }

    private Allocation mapRowToAllocation(ResultSet rs) throws SQLException {
        Allocation a = new Allocation(
            rs.getInt("allocation_id"),
            rs.getString("student_id"),
            rs.getInt("room_id"),
            rs.getTimestamp("allocation_date"),
            rs.getTimestamp("vacating_date"),
            rs.getString("status")
        );
        a.setStudentName(rs.getString("student_name"));
        a.setRoomNumber(rs.getString("room_number"));
        try {
            a.setRoomCapacity(rs.getInt("room_capacity"));
            a.setRoomOccupied(rs.getInt("room_occupied"));
            a.setCoOccupants(rs.getString("co_occupants"));
        } catch (SQLException ignored) {}
        return a;
    }
}
