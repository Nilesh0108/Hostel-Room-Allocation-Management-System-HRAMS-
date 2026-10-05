package com.hrams.web;

import com.hrams.HramsWebApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = HramsWebApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebPortalIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Web REST API: Login Endpoint Test")
    public void testLoginEndpoint() {
        String url = "http://localhost:" + port + "/api/auth/login";
        Map<String, String> creds = new HashMap<>();
        creds.put("username", "admin");
        creds.put("password", "admin123");

        ResponseEntity<Map> response = restTemplate.postForEntity(url, creds, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
    }

    @Test
    @DisplayName("Web REST API: Dashboard Stats Endpoint Test")
    public void testDashboardStatsEndpoint() {
        String url = "http://localhost:" + port + "/api/dashboard/stats";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("totalStudents"));
        assertTrue(response.getBody().containsKey("availableRooms"));
    }
}
