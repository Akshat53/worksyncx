package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.attendance.AttendanceRequest;
import com.worksyncx.hrms.dto.attendance.AttendanceResponse;
import com.worksyncx.hrms.dto.attendance.CheckInRequest;
import com.worksyncx.hrms.dto.attendance.CheckOutRequest;
import com.worksyncx.hrms.service.attendance.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in/{employeeId}")
    public ResponseEntity<?> checkIn(
        @PathVariable Long employeeId,
        @RequestBody CheckInRequest request
    ) {
        try {
            AttendanceResponse response = attendanceService.checkIn(employeeId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to check in", "message", e.getMessage()));
        }
    }

    @PostMapping("/check-out/{employeeId}")
    public ResponseEntity<?> checkOut(
        @PathVariable Long employeeId,
        @RequestBody CheckOutRequest request
    ) {
        try {
            AttendanceResponse response = attendanceService.checkOut(employeeId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to check out", "message", e.getMessage()));
        }
    }

    @PostMapping("/mark")
    public ResponseEntity<?> markAttendance(@Valid @RequestBody AttendanceRequest request) {
        try {
            AttendanceResponse response = attendanceService.markAttendance(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to mark attendance", "message", e.getMessage()));
        }
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getEmployeeAttendance(
        @PathVariable Long employeeId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        try {
            List<AttendanceResponse> records = attendanceService.getEmployeeAttendance(employeeId, startDate, endDate);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to get attendance records", "message", e.getMessage()));
        }
    }

    @GetMapping("/employee/{employeeId}/today")
    public ResponseEntity<?> getTodayAttendance(@PathVariable Long employeeId) {
        try {
            AttendanceResponse response = attendanceService.getTodayAttendance(employeeId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "No attendance record found for today", "message", e.getMessage()));
        }
    }

    @PutMapping("/employee/{employeeId}/date/{date}")
    public ResponseEntity<?> updateAttendance(
        @PathVariable Long employeeId,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @Valid @RequestBody AttendanceRequest request
    ) {
        try {
            AttendanceResponse response = attendanceService.updateAttendance(employeeId, date, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to update attendance", "message", e.getMessage()));
        }
    }
}
