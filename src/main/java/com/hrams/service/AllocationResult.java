package com.hrams.service;

import com.hrams.model.Allocation;
import com.hrams.model.Room;

public class AllocationResult {
    private final boolean success;
    private final boolean queuedToWaitingList;
    private final String message;
    private final Allocation allocation;
    private final Room allocatedRoom;

    public AllocationResult(boolean success, boolean queuedToWaitingList, String message, Allocation allocation, Room allocatedRoom) {
        this.success = success;
        this.queuedToWaitingList = queuedToWaitingList;
        this.message = message;
        this.allocation = allocation;
        this.allocatedRoom = allocatedRoom;
    }

    public static AllocationResult allocated(Allocation allocation, Room room, String message) {
        return new AllocationResult(true, false, message, allocation, room);
    }

    public static AllocationResult queued(String message) {
        return new AllocationResult(true, true, message, null, null);
    }

    public static AllocationResult failed(String message) {
        return new AllocationResult(false, false, message, null, null);
    }

    public boolean isSuccess() { return success; }
    public boolean isQueuedToWaitingList() { return queuedToWaitingList; }
    public String getMessage() { return message; }
    public Allocation getAllocation() { return allocation; }
    public Room getAllocatedRoom() { return allocatedRoom; }
}
