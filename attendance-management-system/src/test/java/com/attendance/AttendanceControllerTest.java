package com.attendance;

import com.attendance.controller.AttendanceController;
import com.attendance.model.CheckInRequest;
import com.attendance.service.AttendanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.attendance.model.CheckIn;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendanceController.class)
public class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetStatus_ReturnsOk() throws Exception {
        when(attendanceService.getTotalCheckedIn()).thenReturn(5);

        mockMvc.perform(get("/attendance/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Attendance Management System"))
                .andExpect(jsonPath("$.totalCheckedIn").value(5));
    }

    @Test
    public void testCheckIn_ValidRequest_ReturnsCreated() throws Exception {
        CheckInRequest request = new CheckInRequest();
        request.setEmployeeId("EMP001");
        request.setEmployeeName("John Doe");
        request.setDepartment("Engineering");

        CheckIn mockResponse = new CheckIn(
            "EMP001", "John Doe", "Engineering",
            LocalDateTime.now(), "SUCCESS", "Check-in successful"
        );

        when(attendanceService.processCheckIn(any(CheckInRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/attendance/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value("EMP001"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    public void testCheckIn_MissingEmployeeId_ReturnsBadRequest() throws Exception {
        CheckInRequest request = new CheckInRequest();
        request.setEmployeeName("John Doe");
        // employeeId intentionally missing

        mockMvc.perform(post("/attendance/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    public void testCheckIn_MissingEmployeeName_ReturnsBadRequest() throws Exception {
        CheckInRequest request = new CheckInRequest();
        request.setEmployeeId("EMP001");
        // employeeName intentionally missing

        mockMvc.perform(post("/attendance/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    public void testGetStatus_ContainsTimestamp() throws Exception {
        when(attendanceService.getTotalCheckedIn()).thenReturn(0);

        mockMvc.perform(get("/attendance/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }
}
