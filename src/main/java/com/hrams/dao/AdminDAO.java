package com.hrams.dao;

import com.hrams.model.Admin;
import com.hrams.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDAO {

    public Admin authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM ADMIN WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Admin(
                        rs.getInt("admin_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        }
        return null;
    }
}
