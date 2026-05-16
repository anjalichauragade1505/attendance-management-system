package com.attendance.service;

import com.attendance.model.CheckIn;
import com.attendance.model.CheckInRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AttendanceService {

    // In-memory store for demo purposes
    private final ConcurrentHashMap<String, CheckIn> attendanceRecord = new ConcurrentHashMap<>();

    /**
     * Process employee check-in
     */
    public CheckIn processCheckIn(CheckInRequest request) {
        LocalDateTime now = LocalDateTime.now();

        // Check if already checked in today
        if (attendanceRecord.containsKey(request.getEmployeeId())) {
            CheckIn existing = attendanceRecord.get(request.getEmployeeId());
            return new CheckIn(
                request.getEmployeeId(),
                request.getEmployeeName(),
                request.getDepartment(),
                existing.getCheckInTime(),
                "ALREADY_CHECKED_IN",
                "Employee " + request.getEmployeeName() + " already checked in at " + existing.getCheckInTime()
            );
        }

        CheckIn checkIn = new CheckIn(
            request.getEmployeeId(),
            request.getEmployeeName(),
            request.getDepartment(),
            now,
            "SUCCESS",
            "Check-in successful for " + request.getEmployeeName() + " at " + now
        );

        attendanceRecord.put(request.getEmployeeId(), checkIn);
        return checkIn;
    }

    /**
     * Get all attendance records
     */
    public List<CheckIn> getAllAttendance() {
        return new ArrayList<>(attendanceRecord.values());
    }

    /**
     * Get total checked-in count
     */
    public int getTotalCheckedIn() {
        return attendanceRecord.size();
    }
}
