package com.worksyncx.hrms.service.attendance;

import com.worksyncx.hrms.dto.attendance.AttendanceRequest;
import com.worksyncx.hrms.dto.attendance.AttendanceResponse;
import com.worksyncx.hrms.dto.attendance.CheckInRequest;
import com.worksyncx.hrms.dto.attendance.CheckOutRequest;
import com.worksyncx.hrms.dto.common.PageResponse;
import com.worksyncx.hrms.dto.shift.ShiftResponse;
import com.worksyncx.hrms.entity.AttendanceRecord;
import com.worksyncx.hrms.entity.Employee;
import com.worksyncx.hrms.enums.AttendanceStatus;
import com.worksyncx.hrms.repository.AttendanceRecordRepository;
import com.worksyncx.hrms.repository.EmployeeRepository;
import com.worksyncx.hrms.security.TenantContext;
import com.worksyncx.hrms.service.shift.ShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftService shiftService;

    @Transactional
    public AttendanceResponse checkIn(Long employeeId, CheckInRequest request) {
        Long tenantId = TenantContext.getTenantId();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Verify employee exists
        Employee employee = employeeRepository.findByTenantIdAndId(tenantId, employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));

        // Check if already checked in today
        attendanceRecordRepository.findByTenantIdAndEmployeeIdAndAttendanceDate(tenantId, employeeId, today)
            .ifPresent(record -> {
                throw new RuntimeException("Employee has already checked in today");
            });

        // Auto-detect employee's shift for today
        Optional<ShiftResponse> shiftOpt = shiftService.getEmployeeShiftForDate(employeeId, today);

        AttendanceRecord record = new AttendanceRecord();
        record.setTenantId(tenantId);
        record.setEmployeeId(employeeId);
        record.setAttendanceDate(today);
        record.setCheckInTime(now);
        record.setLocation(request.getLocation());
        record.setNotes(request.getNotes());
        record.setMarkedBy(TenantContext.getUserId());
        record.setCreatedBy(TenantContext.getUserId());

        // Populate shift information and calculate late/early automatically
        if (shiftOpt.isPresent()) {
            ShiftResponse shift = shiftOpt.get();
            record.setShiftId(shift.getId());
            record.setExpectedStartTime(shift.getStartTime());
            record.setExpectedEndTime(shift.getEndTime());

            // Calculate late arrival
            int minutesLate = shiftService.getMinutesLate(employeeId, today, now);
            record.setLateByMinutes(minutesLate);

            // Set status based on late arrival
            if (minutesLate > 0) {
                record.setStatus(AttendanceStatus.PRESENT); // Still present, but logged as late
                log.info("Employee {} checked in {} minutes late", employee.getEmployeeCode(), minutesLate);
            } else {
                record.setStatus(AttendanceStatus.PRESENT);
                log.info("Employee {} checked in on time", employee.getEmployeeCode());
            }
        } else {
            // No shift assigned, mark as present anyway
            record.setStatus(AttendanceStatus.PRESENT);
            record.setLateByMinutes(0);
            log.warn("No shift assigned to employee {} for date {}", employee.getEmployeeCode(), today);
        }

        record = attendanceRecordRepository.save(record);
        return mapToResponse(record);
    }

    @Transactional
    public AttendanceResponse checkOut(Long employeeId, CheckOutRequest request) {
        Long tenantId = TenantContext.getTenantId();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        AttendanceRecord record = attendanceRecordRepository
            .findByTenantIdAndEmployeeIdAndAttendanceDate(tenantId, employeeId, today)
            .orElseThrow(() -> new RuntimeException("No check-in record found for today"));

        if (record.getCheckOutTime() != null) {
            throw new RuntimeException("Employee has already checked out today");
        }

        record.setCheckOutTime(now);

        // Calculate work hours
        if (record.getCheckInTime() != null) {
            Duration duration = Duration.between(record.getCheckInTime(), now);
            BigDecimal hours = BigDecimal.valueOf(duration.toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            record.setWorkHours(hours);
        }

        // Calculate early leave based on shift
        int minutesEarly = shiftService.getMinutesEarly(employeeId, today, now);
        record.setEarlyLeaveByMinutes(minutesEarly);

        if (minutesEarly > 0) {
            log.info("Employee {} left {} minutes early", employeeId, minutesEarly);
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

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByDate(LocalDate date) {
        Long tenantId = TenantContext.getTenantId();
        return attendanceRecordRepository
            .findByTenantIdAndAttendanceDate(tenantId, date)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
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

    // ==================== Paginated Methods ====================

    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> getEmployeeAttendancePaginated(
        Long employeeId, LocalDate startDate, LocalDate endDate, Pageable pageable
    ) {
        Long tenantId = TenantContext.getTenantId();
        Page<AttendanceRecord> page = attendanceRecordRepository
            .findByTenantIdAndEmployeeIdAndAttendanceDateBetween(tenantId, employeeId, startDate, endDate, pageable);
        return mapToPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> getAttendanceByDatePaginated(LocalDate date, Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<AttendanceRecord> page = attendanceRecordRepository
            .findByTenantIdAndAttendanceDate(tenantId, date, pageable);
        return mapToPageResponse(page);
    }

    // ==================== Mappers ====================

    private PageResponse<AttendanceResponse> mapToPageResponse(Page<AttendanceRecord> page) {
        List<AttendanceResponse> content = page.getContent()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return PageResponse.<AttendanceResponse>builder()
            .content(content)
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .numberOfElements(page.getNumberOfElements())
            .empty(page.isEmpty())
            .build();
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
            .shiftId(record.getShiftId())
            .expectedStartTime(record.getExpectedStartTime())
            .expectedEndTime(record.getExpectedEndTime())
            .lateByMinutes(record.getLateByMinutes())
            .earlyLeaveByMinutes(record.getEarlyLeaveByMinutes())
            .build();
    }
}
