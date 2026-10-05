package com.hrams.algorithm;

import com.hrams.model.Room;
import java.util.List;

/**
 * Implements First-Fit room allocation algorithm.
 * Inspects room list in order and selects the first room with available capacity.
 * Time complexity: O(n) where n is the number of rooms.
 */
public class AllocationAlgorithm {

    public Room findFirstAvailableRoom(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return null;
        }

        for (Room room : rooms) {
            if (room.getOccupiedCount() < room.getCapacity()) {
                return room;
            }
        }
        return null;
    }
}
