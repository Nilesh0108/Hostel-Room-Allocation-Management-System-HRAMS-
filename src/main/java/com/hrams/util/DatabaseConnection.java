package com.hrams.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // MySQL connection settings with 1000ms timeout
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/hrams_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=1000&socketTimeout=2000";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "root";

    // Embedded H2 Database settings with AUTO_SERVER=TRUE for multi-process concurrent access
    private static final String H2_URL = "jdbc:h2:./data/hrams_db;MODE=MySQL;AUTO_SERVER=TRUE;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";

    private static String activeUrl;
    private static String activeUser;
    private static String activePass;
    private static boolean isInitialized = false;

    public static synchronized Connection getConnection() throws SQLException {
        if (!isInitialized) {
            initializeDatabase();
        }
        return DriverManager.getConnection(activeUrl, activeUser, activePass);
    }

    private static void initializeDatabase() {
        // Try MySQL first
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            DriverManager.setLoginTimeout(2);
            Connection conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
            activeUrl = MYSQL_URL;
            activeUser = MYSQL_USER;
            activePass = MYSQL_PASSWORD;
            LOGGER.info("Connected to MySQL Database.");
            conn.close();
            isInitialized = true;
            runSchemaScript();
            seedDefaultData();
            return;
        } catch (Exception e) {
            LOGGER.info("MySQL not available. Falling back to embedded H2 database mode.");
        }

        // Fallback to embedded H2 with AUTO_SERVER mode
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
            activeUrl = H2_URL;
            activeUser = H2_USER;
            activePass = H2_PASSWORD;
            LOGGER.info("Initialized embedded H2 database at ./data/hrams_db");
            conn.close();
            isInitialized = true;
            runSchemaScript();
            seedDefaultData();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database connection!", e);
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    private static void runSchemaScript() {
        try (Connection conn = DriverManager.getConnection(activeUrl, activeUser, activePass);
             Statement stmt = conn.createStatement()) {

            InputStream is = DatabaseConnection.class.getResourceAsStream("/schema.sql");
            if (is == null) {
                LOGGER.warning("schema.sql not found in resources!");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("--") || line.trim().startsWith("//")) {
                    continue;
                }
                sb.append(line).append("\n");
            }

            String[] sqlStatements = sb.toString().split(";");
            for (String sql : sqlStatements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        stmt.execute(trimmed);
                    } catch (SQLException ex) {
                        LOGGER.log(Level.FINE, "SQL Statement execution notice: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing database schema script", e);
        }
    }

    private static void seedDefaultData() {
        try (Connection conn = DriverManager.getConnection(activeUrl, activeUser, activePass)) {
            // Seed Admin (admin / admin123)
            String checkAdmin = "SELECT COUNT(*) FROM ADMIN WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkAdmin)) {
                stmt.setString(1, "admin");
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        String insertAdmin = "INSERT INTO ADMIN (username, password) VALUES (?, ?)";
                        try (PreparedStatement ins = conn.prepareStatement(insertAdmin)) {
                            ins.setString(1, "admin");
                            ins.setString(2, "admin123");
                            ins.executeUpdate();
                            LOGGER.info("Default Admin account seeded: admin / admin123");
                        }
                    }
                }
            }

            // Seed Initial Rooms if empty
            String checkRoom = "SELECT COUNT(*) FROM ROOM";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkRoom)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertRoom = "INSERT INTO ROOM (room_number, capacity, occupied_count, room_type, floor) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement ins = conn.prepareStatement(insertRoom)) {
                        Object[][] rooms = {
                            {"A-101", 2, 0, "Single Deluxe", 1},
                            {"A-102", 2, 0, "Double Standard", 1},
                            {"B-201", 1, 0, "Single Standard", 2},
                            {"B-202", 3, 0, "Triple Suite", 2}
                        };
                        for (Object[] r : rooms) {
                            ins.setString(1, (String) r[0]);
                            ins.setInt(2, (Integer) r[1]);
                            ins.setInt(3, (Integer) r[2]);
                            ins.setString(4, (String) r[3]);
                            ins.setInt(5, (Integer) r[4]);
                            ins.executeUpdate();
                        }
                    }
                }
            }

            // Seed Initial Students if empty
            String checkStudent = "SELECT COUNT(*) FROM STUDENT";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkStudent)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertStudent = "INSERT INTO STUDENT (student_id, name, email, phone, course, year_of_study, gender) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ins = conn.prepareStatement(insertStudent)) {
                        Object[][] students = {
                            {"STU1001", "John Doe", "john.doe@university.edu", "+1-555-0101", "Computer Science", 2, "Male"},
                            {"STU1002", "Jane Smith", "jane.smith@university.edu", "+1-555-0102", "Electrical Eng.", 3, "Female"},
                            {"STU1003", "Alex Johnson", "alex.j@university.edu", "+1-555-0103", "Mechanical Eng.", 1, "Male"},
                            {"STU1004", "Emily Davis", "emily.d@university.edu", "+1-555-0104", "Civil Eng.", 4, "Female"}
                        };
                        for (Object[] s : students) {
                            ins.setString(1, (String) s[0]);
                            ins.setString(2, (String) s[1]);
                            ins.setString(3, (String) s[2]);
                            ins.setString(4, (String) s[3]);
                            ins.setString(5, (String) s[4]);
                            ins.setInt(6, (Integer) s[5]);
                            ins.setString(7, (String) s[6]);
                            ins.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error seeding default data", e);
        }
    }

    public static String getActiveDatabaseType() {
        if (activeUrl != null && activeUrl.contains("h2")) {
            return "Embedded H2 (MySQL Mode)";
        }
        return "MySQL Database";
    }
}
