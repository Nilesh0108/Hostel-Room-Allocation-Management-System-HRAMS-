package com.hrams.web;

import com.hrams.dao.AdminDAO;
import com.hrams.model.Admin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AdminDAO adminDAO = new AdminDAO();

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        Map<String, Object> response = new HashMap<>();
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Username and password are required.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Admin admin = adminDAO.authenticate(username.trim(), password.trim());
            if (admin != null) {
                response.put("success", true);
                response.put("username", admin.getUsername());
                response.put("message", "Authentication successful.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid admin credentials. (Default: admin / admin123)");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Authentication error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
