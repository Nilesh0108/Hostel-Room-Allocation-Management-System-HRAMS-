package com.hrams.dao;

import com.hrams.model.Room;
import com.hrams.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public boolean addRoom(Room room) throws SQLException {
        String sql = "INSERT INTO ROOM (room_number, capacity, occupied_count, room_type, floor) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, room.getRoomNumber());
            stmt.setInt(2, room.getCapacity());
            stmt.setInt(3, room.getOccupiedCount());
            stmt.setString(4, room.getRoomType());
            stmt.setInt(5, room.getFloor());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        room.setRoomId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateRoom(Room room) throws SQLException {
        String sql = "UPDATE ROOM SET room_number = ?, capacity = ?, room_type = ?, floor = ? WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, room.getRoomNumber());
            stmt.setInt(2, room.getCapacity());
            stmt.setString(3, room.getRoomType());
            stmt.setInt(4, room.getFloor());
            stmt.setInt(5, room.getRoomId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteRoom(int roomId) throws SQLException {
        // FR-4.3: allow deleting a room, provided it has no active allocations
        String checkSql = "SELECT COUNT(*) FROM ALLOCATION WHERE room_id = ? AND status = 'ACTIVE'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, roomId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new IllegalStateException("Cannot delete room with active student allocations!");
                }
            }
        }

        String sql = "DELETE FROM ROOM WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Room getRoomById(int roomId) throws SQLException {
        String sql = "SELECT * FROM ROOM WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToRoom(rs);
                }
            }
        }
        return null;
    }

    public Room getRoomByNumber(String roomNumber) throws SQLException {
        String sql = "SELECT * FROM ROOM WHERE room_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToRoom(rs);
                }
            }
        }
        return null;
    }

    public List<Room> getAllRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.*, " +
                     "(SELECT GROUP_CONCAT(s.name SEPARATOR ', ') FROM ALLOCATION a JOIN STUDENT s ON a.student_id = s.student_id WHERE a.room_id = r.room_id AND a.status = 'ACTIVE') as occupants " +
                     "FROM ROOM r ORDER BY r.floor ASC, r.room_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(mapRowToRoom(rs));
            }
        }
        return rooms;
    }

    public boolean incrementOccupancy(int roomId) throws SQLException {
        String sql = "UPDATE ROOM SET occupied_count = occupied_count + 1 WHERE room_id = ? AND occupied_count < capacity";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean decrementOccupancy(int roomId) throws SQLException {
        String sql = "UPDATE ROOM SET occupied_count = GREATEST(0, occupied_count - 1) WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            return stmt.executeUpdate() > 0;
        }
    }

    public int getAvailableRoomCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM ROOM WHERE occupied_count < capacity";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int getOccupiedRoomCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM ROOM WHERE occupied_count >= capacity";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Room mapRowToRoom(ResultSet rs) throws SQLException {
        Room r = new Room(
            rs.getInt("room_id"),
            rs.getString("room_number"),
            rs.getInt("capacity"),
            rs.getInt("occupied_count"),
            rs.getString("room_type"),
            rs.getInt("floor")
        );
        r.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            r.setOccupants(rs.getString("occupants"));
        } catch (SQLException ignored) {}
        return r;
    }
}
