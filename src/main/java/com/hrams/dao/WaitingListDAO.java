package com.hrams.dao;

import com.hrams.model.WaitingEntry;
import com.hrams.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WaitingListDAO {

    public boolean addToWaitingList(String studentId) throws SQLException {
        if (isStudentOnWaitingList(studentId)) {
            return false; // Already queued
        }
        String sql = "INSERT INTO WAITING_LIST (student_id, request_date, status) VALUES (?, CURRENT_TIMESTAMP, 'WAITING')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean isStudentOnWaitingList(String studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM WAITING_LIST WHERE student_id = ? AND status = 'WAITING'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public WaitingEntry getNextWaitingStudent() throws SQLException {
        String sql = "SELECT w.*, s.name as student_name, s.course, s.gender FROM WAITING_LIST w " +
                     "JOIN STUDENT s ON w.student_id = s.student_id " +
                     "WHERE w.status = 'WAITING' ORDER BY w.request_date ASC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return mapRowToWaitingEntry(rs);
            }
        }
        return null;
    }

    public List<WaitingEntry> getWaitingList() throws SQLException {
        List<WaitingEntry> list = new ArrayList<>();
        String sql = "SELECT w.*, s.name as student_name, s.course, s.gender FROM WAITING_LIST w " +
                     "JOIN STUDENT s ON w.student_id = s.student_id " +
                     "WHERE w.status = 'WAITING' ORDER BY w.request_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToWaitingEntry(rs));
            }
        }
        return list;
    }

    public List<WaitingEntry> getAllWaitingHistory() throws SQLException {
        List<WaitingEntry> list = new ArrayList<>();
        String sql = "SELECT w.*, s.name as student_name, s.course, s.gender FROM WAITING_LIST w " +
                     "JOIN STUDENT s ON w.student_id = s.student_id " +
                     "ORDER BY w.request_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToWaitingEntry(rs));
            }
        }
        return list;
    }

    public boolean updateStatus(int waitingId, String status) throws SQLException {
        String sql = "UPDATE WAITING_LIST SET status = ? WHERE waiting_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, waitingId);
            return stmt.executeUpdate() > 0;
        }
    }

    public int getWaitingCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM WAITING_LIST WHERE status = 'WAITING'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private WaitingEntry mapRowToWaitingEntry(ResultSet rs) throws SQLException {
        WaitingEntry w = new WaitingEntry(
            rs.getInt("waiting_id"),
            rs.getString("student_id"),
            rs.getTimestamp("request_date"),
            rs.getString("status")
        );
        w.setStudentName(rs.getString("student_name"));
        w.setStudentCourse(rs.getString("course"));
        w.setStudentGender(rs.getString("gender"));
        return w;
    }
}
