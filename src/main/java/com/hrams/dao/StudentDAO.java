package com.hrams.dao;

import com.hrams.model.Student;
import com.hrams.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public boolean addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO STUDENT (student_id, name, email, phone, course, year_of_study, gender) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getStudentId());
            stmt.setString(2, student.getName());
            stmt.setString(3, student.getEmail());
            stmt.setString(4, student.getPhone());
            stmt.setString(5, student.getCourse());
            stmt.setInt(6, student.getYearOfStudy());
            stmt.setString(7, student.getGender());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateStudent(Student student) throws SQLException {
        String sql = "UPDATE STUDENT SET name = ?, email = ?, phone = ?, course = ?, year_of_study = ?, gender = ? WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, student.getName());
            stmt.setString(2, student.getEmail());
            stmt.setString(3, student.getPhone());
            stmt.setString(4, student.getCourse());
            stmt.setInt(5, student.getYearOfStudy());
            stmt.setString(6, student.getGender());
            stmt.setString(7, student.getStudentId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteStudent(String studentId) throws SQLException {
        String sql = "DELETE FROM STUDENT WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Student getStudentById(String studentId) throws SQLException {
        String sql = "SELECT * FROM STUDENT WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStudent(rs);
                }
            }
        }
        return null;
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM STUDENT ORDER BY student_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(mapRowToStudent(rs));
            }
        }
        return students;
    }

    public List<Student> searchStudents(String query) throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM STUDENT WHERE LOWER(student_id) LIKE ? OR LOWER(name) LIKE ? ORDER BY student_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String pattern = "%" + query.toLowerCase() + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapRowToStudent(rs));
                }
            }
        }
        return students;
    }

    public int getTotalStudentCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM STUDENT";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Student mapRowToStudent(ResultSet rs) throws SQLException {
        Student s = new Student(
            rs.getString("student_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("course"),
            rs.getInt("year_of_study"),
            rs.getString("gender")
        );
        s.setCreatedAt(rs.getTimestamp("created_at"));
        return s;
    }
}
