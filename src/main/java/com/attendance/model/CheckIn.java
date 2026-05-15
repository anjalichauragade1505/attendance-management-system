package com.attendance.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class CheckIn {

    private String employeeId;
    private String employeeName;
    private String department;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;

    private String status;
    private String message;

    // Default constructor
    public CheckIn() {}

    // Constructor for response
    public CheckIn(String employeeId, String employeeName, String department,
                   LocalDateTime checkInTime, String status, String message) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.checkInTime = checkInTime;
        this.status = status;
        this.message = message;
    }

    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
