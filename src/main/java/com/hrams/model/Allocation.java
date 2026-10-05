package com.hrams.model;

import java.sql.Timestamp;

public class Allocation {
    private int allocationId;
    private String studentId;
    private int roomId;
    private Timestamp allocationDate;
    private Timestamp vacatingDate;
    private String status; // "ACTIVE" or "VACATED"

    // Joined fields for view convenience
    private String studentName;
    private String roomNumber;
    private int roomCapacity;
    private int roomOccupied;
    private String coOccupants; // Names of all students sharing this room

    public Allocation() {}

    public Allocation(int allocationId, String studentId, int roomId, Timestamp allocationDate, Timestamp vacatingDate, String status) {
        this.allocationId = allocationId;
        this.studentId = studentId;
        this.roomId = roomId;
        this.allocationDate = allocationDate;
        this.vacatingDate = vacatingDate;
        this.status = status;
    }

    public int getAllocationId() { return allocationId; }
    public void setAllocationId(int allocationId) { this.allocationId = allocationId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public Timestamp getAllocationDate() { return allocationDate; }
    public void setAllocationDate(Timestamp allocationDate) { this.allocationDate = allocationDate; }

    public Timestamp getVacatingDate() { return vacatingDate; }
    public void setVacatingDate(Timestamp vacatingDate) { this.vacatingDate = vacatingDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getRoomCapacity() { return roomCapacity; }
    public void setRoomCapacity(int roomCapacity) { this.roomCapacity = roomCapacity; }

    public int getRoomOccupied() { return roomOccupied; }
    public void setRoomOccupied(int roomOccupied) { this.roomOccupied = roomOccupied; }

    public String getCoOccupants() { return coOccupants != null ? coOccupants : ""; }
    public void setCoOccupants(String coOccupants) { this.coOccupants = coOccupants; }

    public String getRoomCapacityDisplay() {
        if (roomNumber == null) return "";
        return roomNumber + " (" + roomOccupied + "/" + roomCapacity + " Beds)";
    }
}
