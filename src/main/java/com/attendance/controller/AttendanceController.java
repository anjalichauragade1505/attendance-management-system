package com.attendance.controller;

import com.attendance.model.CheckIn;
import com.attendance.model.CheckInRequest;
import com.attendance.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    /**
     * GET /attendance/status
     * Returns service health and current status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Attendance Management System");
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("version", "1.0.0");
        response.put("totalCheckedIn", attendanceService.getTotalCheckedIn());
        response.put("message", "Service is running healthy");

        return ResponseEntity.ok(response);
    }

    /**
     * POST /attendance/checkin
     * Simulates a user check-in
     */
    @PostMapping("/checkin")
    public ResponseEntity<CheckIn> checkIn(@RequestBody CheckInRequest request) {
        if (request.getEmployeeId() == null || request.getEmployeeId().isEmpty()) {
            CheckIn error = new CheckIn(
                null, null, null, null,
                "FAILED",
                "Employee ID is required"
            );
            return ResponseEntity.badRequest().body(error);
        }

        if (request.getEmployeeName() == null || request.getEmployeeName().isEmpty()) {
            CheckIn error = new CheckIn(
                null, null, null, null,
                "FAILED",
                "Employee Name is required"
            );
            return ResponseEntity.badRequest().body(error);
        }

        CheckIn result = attendanceService.processCheckIn(request);

        if ("SUCCESS".equals(result.getStatus())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } else {
            return ResponseEntity.ok(result);
        }
    }

    /**
     * GET /attendance/records
     * Returns all attendance records (bonus endpoint)
     */
    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> getAllRecords() {
        List<CheckIn> records = attendanceService.getAllAttendance();
        Map<String, Object> response = new HashMap<>();
        response.put("totalRecords", records.size());
        response.put("records", records);
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }
}
