package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.annotation.RequiresModule;
import com.worksyncx.hrms.dto.attendance.AttendanceRequest;
import com.worksyncx.hrms.dto.attendance.AttendanceResponse;
import com.worksyncx.hrms.dto.attendance.CheckInRequest;
import com.worksyncx.hrms.dto.attendance.CheckOutRequest;
import com.worksyncx.hrms.dto.common.PageResponse;
import com.worksyncx.hrms.enums.Module;
import com.worksyncx.hrms.service.attendance.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_EMPLOYEE')")
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
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_EMPLOYEE')")
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
    @RequiresModule(Module.ATTENDANCE)
    @PreAuthorize("hasAuthority('ATTENDANCE:MARK')")
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
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_EMPLOYEE')")
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
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> getTodayAttendance(@PathVariable Long employeeId) {
        try {
            AttendanceResponse response = attendanceService.getTodayAttendance(employeeId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "No attendance record found for today", "message", e.getMessage()));
        }
    }

    @GetMapping("/today")
    @RequiresModule(Module.ATTENDANCE)
    @PreAuthorize("hasAuthority('ATTENDANCE:READ')")
    public ResponseEntity<?> getAllTodayAttendance() {
        try {
            LocalDate today = LocalDate.now();
            List<AttendanceResponse> records = attendanceService.getAttendanceByDate(today);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to get today's attendance", "message", e.getMessage()));
        }
    }

    @GetMapping("/date/{date}")
    @RequiresModule(Module.ATTENDANCE)
    @PreAuthorize("hasAuthority('ATTENDANCE:READ')")
    public ResponseEntity<?> getAttendanceByDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            List<AttendanceResponse> records = attendanceService.getAttendanceByDate(date);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to get attendance for date", "message", e.getMessage()));
        }
    }

    @PutMapping("/employee/{employeeId}/date/{date}")
    @RequiresModule(Module.ATTENDANCE)
    @PreAuthorize("hasAuthority('ATTENDANCE:UPDATE')")
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

    @GetMapping("/employee/{employeeId}/page")
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> getEmployeeAttendancePaginated(
        @PathVariable Long employeeId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "attendanceDate") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        try {
            // Create sort object
            Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, sort);

            PageResponse<AttendanceResponse> recordsPage = attendanceService
                .getEmployeeAttendancePaginated(employeeId, startDate, endDate, pageable);

            return ResponseEntity.ok(recordsPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to get attendance records", "message", e.getMessage()));
        }
    }

    @GetMapping("/date/{date}/page")
    @RequiresModule(Module.ATTENDANCE)
    @PreAuthorize("hasAuthority('ATTENDANCE:READ')")
    public ResponseEntity<?> getAttendanceByDatePaginated(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        try {
            // Create sort object
            Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, sort);

            PageResponse<AttendanceResponse> recordsPage = attendanceService
                .getAttendanceByDatePaginated(date, pageable);

            return ResponseEntity.ok(recordsPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to get attendance for date", "message", e.getMessage()));
        }
    }
}
