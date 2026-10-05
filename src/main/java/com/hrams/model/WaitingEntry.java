package com.hrams.model;

import java.sql.Timestamp;

public class WaitingEntry {
    private int waitingId;
    private String studentId;
    private Timestamp requestDate;
    private String status; // "WAITING", "ALLOCATED", "CANCELLED"

    // Joined fields for UI convenience
    private String studentName;
    private String studentCourse;
    private String studentGender;

    public WaitingEntry() {}

    public WaitingEntry(int waitingId, String studentId, Timestamp requestDate, String status) {
        this.waitingId = waitingId;
        this.studentId = studentId;
        this.requestDate = requestDate;
        this.status = status;
    }

    public int getWaitingId() { return waitingId; }
    public void setWaitingId(int waitingId) { this.waitingId = waitingId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public Timestamp getRequestDate() { return requestDate; }
    public void setRequestDate(Timestamp requestDate) { this.requestDate = requestDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentCourse() { return studentCourse; }
    public void setStudentCourse(String studentCourse) { this.studentCourse = studentCourse; }

    public String getStudentGender() { return studentGender; }
    public void setStudentGender(String studentGender) { this.studentGender = studentGender; }
}
