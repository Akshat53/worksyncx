package com.worksyncx.hrms.service.attendance;

import com.worksyncx.hrms.dto.attendance.AttendanceRequest;
import com.worksyncx.hrms.dto.attendance.AttendanceResponse;
import com.worksyncx.hrms.dto.attendance.CheckInRequest;
import com.worksyncx.hrms.dto.attendance.CheckOutRequest;
import com.worksyncx.hrms.entity.AttendanceRecord;
import com.worksyncx.hrms.entity.Employee;
import com.worksyncx.hrms.enums.AttendanceStatus;
import com.worksyncx.hrms.repository.AttendanceRecordRepository;
import com.worksyncx.hrms.repository.EmployeeRepository;
import com.worksyncx.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AttendanceResponse checkIn(Long employeeId, CheckInRequest request) {
        Long tenantId = TenantContext.getTenantId();
        LocalDate today = LocalDate.now();

        // Verify employee exists
        Employee employee = employeeRepository.findByTenantIdAndId(tenantId, employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        // Check if already checked in today
        attendanceRecordRepository.findByTenantIdAndEmployeeIdAndAttendanceDate(tenantId, employeeId, today)
            .ifPresent(record -> {
                throw new RuntimeException("Employee has already checked in today");
            });

        AttendanceRecord record = new AttendanceRecord();
        record.setTenantId(tenantId);
        record.setEmployeeId(employeeId);
        record.setAttendanceDate(today);
        record.setCheckInTime(LocalTime.now());
        record.setStatus(AttendanceStatus.PRESENT);
        record.setLocation(request.getLocation());
        record.setNotes(request.getNotes());
        record.setMarkedBy(TenantContext.getUserId());
        record.setCreatedBy(TenantContext.getUserId());

        record = attendanceRecordRepository.save(record);
        return mapToResponse(record);
    }

    @Transactional
    public AttendanceResponse checkOut(Long employeeId, CheckOutRequest request) {
        Long tenantId = TenantContext.getTenantId();
        LocalDate today = LocalDate.now();

        AttendanceRecord record = attendanceRecordRepository
            .findByTenantIdAndEmployeeIdAndAttendanceDate(tenantId, employeeId, today)
            .orElseThrow(() -> new RuntimeException("No check-in record found for today"));

        if (record.getCheckOutTime() != null) {
            throw new RuntimeException("Employee has already checked out today");
        }

        LocalTime checkOutTime = LocalTime.now();
        record.setCheckOutTime(checkOutTime);

        // Calculate work hours
        if (record.getCheckInTime() != null) {
            Duration duration = Duration.between(record.getCheckInTime(), checkOutTime);
            BigDecimal hours = BigDecimal.valueOf(duration.toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            record.setWorkHours(hours);
        }

        if (request.getNotes() != null) {
            String existingNotes = record.getNotes() != null ? record.getNotes() + "\n" : "";
            record.setNotes(existingNotes + request.getNotes());
        }

        record.setUpdatedBy(TenantContext.getUserId());
        record = attendanceRecordRepository.save(record);
        return mapToResponse(record);
    }

    @Transactional
    public AttendanceResponse markAttendance(AttendanceRequest request) {
        Long tenantId = TenantContext.getTenantId();

        // Verify employee exists
        employeeRepository.findByTenantIdAndId(tenantId, request.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + request.getEmployeeId()));

        // Check if attendance already exists for this date
        attendanceRecordRepository.findByTenantIdAndEmployeeIdAndAttendanceDate(
            tenantId, request.getEmployeeId(), request.getAttendanceDate()
        ).ifPresent(record -> {
            throw new RuntimeException("Attendance already marked for this date");
        });

        AttendanceRecord record = new AttendanceRecord();
        record.setTenantId(tenantId);
        record.setEmployeeId(request.getEmployeeId());
        record.setAttendanceDate(request.getAttendanceDate());
        record.setCheckInTime(request.getCheckInTime());
        record.setCheckOutTime(request.getCheckOutTime());
        record.setWorkHours(request.getWorkHours());
        record.setStatus(request.getStatus() != null ? request.getStatus() : AttendanceStatus.PRESENT);
        record.setLocation(request.getLocation());
        record.setNotes(request.getNotes());
        record.setMarkedBy(TenantContext.getUserId());
        record.setCreatedBy(TenantContext.getUserId());

        record = attendanceRecordRepository.save(record);
        return mapToResponse(record);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getEmployeeAttendance(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Long tenantId = TenantContext.getTenantId();
        return attendanceRecordRepository
            .findByTenantIdAndEmployeeIdAndAttendanceDateBetween(tenantId, employeeId, startDate, endDate)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getTodayAttendance(Long employeeId) {
        Long tenantId = TenantContext.getTenantId();
        LocalDate today = LocalDate.now();

        AttendanceRecord record = attendanceRecordRepository
            .findByTenantIdAndEmployeeIdAndAttendanceDate(tenantId, employeeId, today)
            .orElseThrow(() -> new RuntimeException("No attendance record found for today"));

        return mapToResponse(record);
    }

    @Transactional
    public AttendanceResponse updateAttendance(Long employeeId, LocalDate date, AttendanceRequest request) {
        Long tenantId = TenantContext.getTenantId();

        AttendanceRecord record = attendanceRecordRepository
            .findByTenantIdAndEmployeeIdAndAttendanceDate(tenantId, employeeId, date)
            .orElseThrow(() -> new RuntimeException("Attendance record not found for the specified date"));

        record.setCheckInTime(request.getCheckInTime());
        record.setCheckOutTime(request.getCheckOutTime());
        record.setWorkHours(request.getWorkHours());
        record.setStatus(request.getStatus());
        record.setLocation(request.getLocation());
        record.setNotes(request.getNotes());
        record.setUpdatedBy(TenantContext.getUserId());

        record = attendanceRecordRepository.save(record);
        return mapToResponse(record);
    }

    private AttendanceResponse mapToResponse(AttendanceRecord record) {
        return AttendanceResponse.builder()
            .id(record.getId())
            .tenantId(record.getTenantId())
            .employeeId(record.getEmployeeId())
            .attendanceDate(record.getAttendanceDate())
            .checkInTime(record.getCheckInTime())
            .checkOutTime(record.getCheckOutTime())
            .workHours(record.getWorkHours())
            .status(record.getStatus())
            .location(record.getLocation())
            .notes(record.getNotes())
            .markedBy(record.getMarkedBy())
            .createdAt(record.getCreatedAt())
            .updatedAt(record.getUpdatedAt())
            .createdBy(record.getCreatedBy())
            .updatedBy(record.getUpdatedBy())
            .build();
    }
}
