package com.hrams.web;

import com.hrams.dao.StudentDAO;
import com.hrams.model.Student;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentApiController {

    private final StudentDAO studentDAO = new StudentDAO();

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents(@RequestParam(value = "query", required = false) String query) {
        try {
            if (query != null && !query.trim().isEmpty()) {
                return ResponseEntity.ok(studentDAO.searchStudents(query.trim()));
            }
            return ResponseEntity.ok(studentDAO.getAllStudents());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable("id") String id) {
        try {
            Student student = studentDAO.getStudentById(id);
            if (student != null) {
                return ResponseEntity.ok(student);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addStudent(@RequestBody Student student) {
        Map<String, Object> res = new HashMap<>();
        try {
            if (student.getStudentId() == null || student.getName() == null || student.getStudentId().trim().isEmpty() || student.getName().trim().isEmpty()) {
                res.put("success", false);
                res.put("message", "Student ID and Name are required.");
                return ResponseEntity.badRequest().body(res);
            }
            if (studentDAO.getStudentById(student.getStudentId().trim()) != null) {
                res.put("success", false);
                res.put("message", "Student ID '" + student.getStudentId() + "' already exists!");
                return ResponseEntity.badRequest().body(res);
            }
            boolean added = studentDAO.addStudent(student);
            res.put("success", added);
            res.put("message", added ? "Student record added successfully." : "Could not add student.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Database error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateStudent(@PathVariable("id") String id, @RequestBody Student student) {
        Map<String, Object> res = new HashMap<>();
        try {
            student.setStudentId(id);
            boolean updated = studentDAO.updateStudent(student);
            res.put("success", updated);
            res.put("message", updated ? "Student record updated successfully." : "Student not found.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Database error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteStudent(@PathVariable("id") String id) {
        Map<String, Object> res = new HashMap<>();
        try {
            boolean deleted = studentDAO.deleteStudent(id);
            res.put("success", deleted);
            res.put("message", deleted ? "Student deleted successfully." : "Student not found.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Delete error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }
}
