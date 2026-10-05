package com.hrams.algorithm;

import com.hrams.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AllocationAlgorithmTest {

    private AllocationAlgorithm algorithm;

    @BeforeEach
    public void setUp() {
        algorithm = new AllocationAlgorithm();
    }

    @Test
    @DisplayName("First-Fit: Should return the first room with free capacity")
    public void testFindFirstAvailableRoomSuccess() {
        List<Room> rooms = new ArrayList<>();
        // Room 1: Full (cap 2, occ 2)
        rooms.add(new Room(1, "101", 2, 2, "Standard", 1));
        // Room 2: Available (cap 2, occ 1) -> First-Fit candidate
        rooms.add(new Room(2, "102", 2, 1, "Standard", 1));
        // Room 3: Available (cap 2, occ 0)
        rooms.add(new Room(3, "103", 2, 0, "Standard", 1));

        Room selected = algorithm.findFirstAvailableRoom(rooms);
        assertNotNull(selected, "Selected room should not be null");
        assertEquals("102", selected.getRoomNumber(), "First-fit algorithm should select room 102");
    }

    @Test
    @DisplayName("First-Fit: Should return null when all rooms are at full capacity")
    public void testFindFirstAvailableRoomAllFull() {
        List<Room> rooms = new ArrayList<>();
        rooms.add(new Room(1, "101", 2, 2, "Standard", 1));
        rooms.add(new Room(2, "102", 1, 1, "Single", 1));

        Room selected = algorithm.findFirstAvailableRoom(rooms);
        assertNull(selected, "Should return null when all rooms are full");
    }

    @Test
    @DisplayName("First-Fit: Should return null for empty or null room list")
    public void testFindFirstAvailableRoomEmptyList() {
        assertNull(algorithm.findFirstAvailableRoom(null));
        assertNull(algorithm.findFirstAvailableRoom(new ArrayList<>()));
    }
}
