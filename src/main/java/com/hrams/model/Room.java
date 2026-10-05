package com.hrams.model;

import java.sql.Timestamp;

public class Room {
    private int roomId;
    private String roomNumber;
    private int capacity;
    private int occupiedCount;
    private String roomType;
    private int floor;
    private Timestamp createdAt;

    public Room() {}

    public Room(int roomId, String roomNumber, int capacity, int occupiedCount, String roomType, int floor) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.occupiedCount = occupiedCount;
        this.roomType = roomType;
        this.floor = floor;
    }

    public Room(String roomNumber, int capacity, String roomType, int floor) {
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.occupiedCount = 0;
        this.roomType = roomType;
        this.floor = floor;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getOccupiedCount() { return occupiedCount; }
    public void setOccupiedCount(int occupiedCount) { this.occupiedCount = occupiedCount; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public int getFloor() { return floor; }
    public void setFloor(int floor) { this.floor = floor; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isAvailable() {
        return occupiedCount < capacity;
    }

    public String getStatus() {
        if (occupiedCount >= capacity) {
            return "Full";
        } else if (occupiedCount > 0) {
            return "Partially Occupied";
        } else {
            return "Available";
        }
    }

    public int getAvailableBeds() {
        return Math.max(0, capacity - occupiedCount);
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (Cap: " + capacity + ", Occ: " + occupiedCount + ")";
    }
}
