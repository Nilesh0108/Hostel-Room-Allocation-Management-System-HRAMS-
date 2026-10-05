package com.hrams.service;

import com.hrams.algorithm.AllocationAlgorithm;
import com.hrams.dao.AllocationDAO;
import com.hrams.dao.RoomDAO;
import com.hrams.dao.StudentDAO;
import com.hrams.dao.WaitingListDAO;
import com.hrams.model.Allocation;
import com.hrams.model.Room;
import com.hrams.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class AllocationServiceTest {

    private AllocationService allocationService;

    @BeforeEach
    public void setUp() {
        allocationService = new AllocationService();
    }

    @Test
    @DisplayName("Allocation: Non-existent student ID should fail gracefully")
    public void testAllocateNonExistentStudent() throws SQLException {
        AllocationResult result = allocationService.allocateStudent("NON_EXISTENT_ID_999");
        assertFalse(result.isSuccess(), "Allocation should fail for invalid student ID");
        assertTrue(result.getMessage().contains("does not exist"), "Error message should indicate missing student");
    }
}
