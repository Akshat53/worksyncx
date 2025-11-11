package com.worksyncx.hrms.service.shift;

import com.worksyncx.hrms.dto.common.PageResponse;
import com.worksyncx.hrms.dto.shift.*;
import com.worksyncx.hrms.entity.Employee;
import com.worksyncx.hrms.entity.EmployeeShift;
import com.worksyncx.hrms.entity.Shift;
import com.worksyncx.hrms.exception.DuplicateShiftCodeException;
import com.worksyncx.hrms.exception.InvalidTimeRangeException;
import com.worksyncx.hrms.exception.InvalidDateException;
import com.worksyncx.hrms.repository.EmployeeRepository;
import com.worksyncx.hrms.repository.EmployeeShiftRepository;
import com.worksyncx.hrms.repository.ShiftRepository;
import com.worksyncx.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final EmployeeShiftRepository employeeShiftRepository;
    private final EmployeeRepository employeeRepository;

    // ==================== Shift CRUD Operations ====================

    @Transactional
    public ShiftResponse createShift(ShiftRequest request) {
        Long tenantId = TenantContext.getTenantId();

        // Validate unique code (excluding soft-deleted shifts)
        if (shiftRepository.existsByTenantIdAndCodeExcludingDeleted(tenantId, request.getCode())) {
            throw new DuplicateShiftCodeException("A shift with code '" + request.getCode() + "' already exists for your organization");
        }

        // Validate time range - allow night shifts that cross midnight
        if (request.getEndTime().equals(request.getStartTime())) {
            throw new InvalidTimeRangeException("End time cannot be the same as start time");
        }

        Shift shift = new Shift();
        shift.setTenantId(tenantId);
        shift.setName(request.getName());
        shift.setCode(request.getCode());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setGracePeriodMinutes(request.getGracePeriodMinutes());
        shift.setHalfDayHours(request.getHalfDayHours());
        shift.setFullDayHours(request.getFullDayHours());
        shift.setColor(request.getColor());
        shift.setDescription(request.getDescription());
        shift.setIsActive(request.getIsActive());

        shift = shiftRepository.save(shift);
        log.info("Created shift: {} for tenant: {}", shift.getName(), tenantId);

        return toShiftResponse(shift);
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> getAllShifts() {
        Long tenantId = TenantContext.getTenantId();
        return shiftRepository.findByTenantId(tenantId).stream()
                .map(this::toShiftResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> getActiveShifts() {
        Long tenantId = TenantContext.getTenantId();
        return shiftRepository.findByTenantIdAndIsActive(tenantId, true).stream()
                .map(this::toShiftResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ShiftResponse getShiftById(Long shiftId) {
        Shift shift = findShiftByIdAndTenant(shiftId);
        return toShiftResponse(shift);
    }

    @Transactional
    public ShiftResponse updateShift(Long shiftId, ShiftRequest request) {
        Shift shift = findShiftByIdAndTenant(shiftId);

        // Validate code uniqueness if changed (excluding soft-deleted shifts)
        if (!shift.getCode().equals(request.getCode()) &&
            shiftRepository.existsByTenantIdAndCodeExcludingDeleted(shift.getTenantId(), request.getCode())) {
            throw new DuplicateShiftCodeException("A shift with code '" + request.getCode() + "' already exists for your organization");
        }

        // Validate time range - allow night shifts that cross midnight
        if (request.getEndTime().equals(request.getStartTime())) {
            throw new InvalidTimeRangeException("End time cannot be the same as start time");
        }

        shift.setName(request.getName());
        shift.setCode(request.getCode());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setGracePeriodMinutes(request.getGracePeriodMinutes());
        shift.setHalfDayHours(request.getHalfDayHours());
        shift.setFullDayHours(request.getFullDayHours());
        shift.setColor(request.getColor());
        shift.setDescription(request.getDescription());
        shift.setIsActive(request.getIsActive());

        shift = shiftRepository.save(shift);
        log.info("Updated shift: {}", shift.getName());

        return toShiftResponse(shift);
    }

    @Transactional
    public void deleteShift(Long shiftId) {
        Shift shift = findShiftByIdAndTenant(shiftId);

        // Soft delete: Mark as deleted instead of removing from database
        shift.setIsDeleted(true);
        shift.setDeletedAt(LocalDateTime.now());
        shift.setDeletedBy(TenantContext.getUserId());

        shiftRepository.save(shift);
        log.info("Soft deleted shift: {} by user: {}", shift.getName(), TenantContext.getUserId());
    }

    @Transactional(readOnly = true)
    public PageResponse<ShiftResponse> getAllShiftsPaginated(Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<Shift> page = shiftRepository.findByTenantId(tenantId, pageable);
        return mapToPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<ShiftResponse> getActiveShiftsPaginated(Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<Shift> page = shiftRepository.findByTenantIdAndIsActive(tenantId, true, pageable);
        return mapToPageResponse(page);
    }

    // ==================== Employee Shift Assignment ====================

    @Transactional
    public EmployeeShiftResponse assignShiftToEmployee(AssignShiftRequest request) {
        Long tenantId = TenantContext.getTenantId();

        // Validate employee and shift existence
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Employee not found in your organization");
        }

        Shift shift = findShiftByIdAndTenant(request.getShiftId());

        // Validate date range
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new InvalidDateException("Effective to date must be after effective from date");
        }

        // Check for overlapping shifts
        LocalDate effectiveTo = request.getEffectiveTo() != null ? request.getEffectiveTo() : LocalDate.now().plusYears(100);
        List<EmployeeShift> overlapping = employeeShiftRepository.findOverlappingShifts(
                request.getEmployeeId(),
                request.getEffectiveFrom(),
                effectiveTo,
                0L
        );

        if (!overlapping.isEmpty()) {
            // Auto-deactivate or adjust conflicting shifts
            log.info("Found {} overlapping shift assignments. Auto-deactivating them.", overlapping.size());
            for (EmployeeShift overlap : overlapping) {
                // If the new shift completely covers the old one, deactivate it
                if (request.getEffectiveFrom().isBefore(overlap.getEffectiveFrom()) ||
                    request.getEffectiveFrom().equals(overlap.getEffectiveFrom())) {
                    overlap.setIsActive(false);
                    employeeShiftRepository.save(overlap);
                } else {
                    // Adjust the end date of the old shift
                    overlap.setEffectiveTo(request.getEffectiveFrom().minusDays(1));
                    employeeShiftRepository.save(overlap);
                }
            }
        }

        // Create new shift assignment
        EmployeeShift employeeShift = new EmployeeShift();
        employeeShift.setEmployeeId(request.getEmployeeId());
        employeeShift.setShiftId(request.getShiftId());
        employeeShift.setEffectiveFrom(request.getEffectiveFrom());
        employeeShift.setEffectiveTo(request.getEffectiveTo());
        employeeShift.setDaysOfWeek(request.getDaysOfWeek().toArray(new String[0]));
        employeeShift.setIsActive(request.getIsActive());
        employeeShift.setNotes(request.getNotes());

        employeeShift = employeeShiftRepository.save(employeeShift);
        log.info("Assigned shift {} to employee {} effective from {}",
                shift.getName(), employee.getEmployeeCode(), request.getEffectiveFrom());

        return toEmployeeShiftResponse(employeeShift, employee, shift);
    }

    @Transactional(readOnly = true)
    public List<EmployeeShiftResponse> getEmployeeShifts(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<EmployeeShift> shifts = employeeShiftRepository.findByEmployeeId(employeeId);

        return shifts.stream()
                .map(es -> {
                    Shift shift = shiftRepository.findById(es.getShiftId()).orElse(null);
                    return toEmployeeShiftResponse(es, employee, shift);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeShiftResponse updateShiftAssignment(Long assignmentId, AssignShiftRequest request) {
        EmployeeShift employeeShift = employeeShiftRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Shift assignment not found"));

        // Validate employee and shift
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Shift shift = findShiftByIdAndTenant(request.getShiftId());

        // Validate date range
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new InvalidDateException("Effective to date must be after effective from date");
        }

        // Check for overlapping shifts (exclude current assignment)
        LocalDate effectiveTo = request.getEffectiveTo() != null ? request.getEffectiveTo() : LocalDate.now().plusYears(100);
        List<EmployeeShift> overlapping = employeeShiftRepository.findOverlappingShifts(
                request.getEmployeeId(),
                request.getEffectiveFrom(),
                effectiveTo,
                assignmentId
        );

        if (!overlapping.isEmpty()) {
            throw new RuntimeException("This shift assignment overlaps with existing assignments");
        }

        // Update assignment
        employeeShift.setEmployeeId(request.getEmployeeId());
        employeeShift.setShiftId(request.getShiftId());
        employeeShift.setEffectiveFrom(request.getEffectiveFrom());
        employeeShift.setEffectiveTo(request.getEffectiveTo());
        employeeShift.setDaysOfWeek(request.getDaysOfWeek().toArray(new String[0]));
        employeeShift.setIsActive(request.getIsActive());
        employeeShift.setNotes(request.getNotes());

        employeeShift = employeeShiftRepository.save(employeeShift);
        log.info("Updated shift assignment {} for employee {}", assignmentId, employee.getEmployeeCode());

        return toEmployeeShiftResponse(employeeShift, employee, shift);
    }

    @Transactional
    public void deleteShiftAssignment(Long assignmentId) {
        EmployeeShift employeeShift = employeeShiftRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Shift assignment not found"));

        employeeShiftRepository.delete(employeeShift);
        log.info("Deleted shift assignment: {}", assignmentId);
    }

    @Transactional(readOnly = true)
    public List<EmployeeShiftResponse> getEmployeesOnShift(Long shiftId, LocalDate date) {
        Shift shift = findShiftByIdAndTenant(shiftId);
        List<EmployeeShift> employeeShifts = employeeShiftRepository.findEmployeesOnShiftOnDate(shiftId, date);

        return employeeShifts.stream()
                .map(es -> {
                    Employee employee = employeeRepository.findById(es.getEmployeeId()).orElse(null);
                    return toEmployeeShiftResponse(es, employee, shift);
                })
                .collect(Collectors.toList());
    }

    // ==================== Automated Shift Detection ====================

    /**
     * Automatically detect and return the active shift for an employee on a given date
     * This is the core automation feature used by attendance system
     */
    @Transactional(readOnly = true)
    public Optional<ShiftResponse> getEmployeeShiftForDate(Long employeeId, LocalDate date) {
        String dayOfWeek = date.getDayOfWeek().name();

        Optional<EmployeeShift> employeeShift = employeeShiftRepository
                .findActiveShiftForEmployeeOnDateAndDay(employeeId, date, dayOfWeek);

        if (employeeShift.isPresent()) {
            Shift shift = shiftRepository.findById(employeeShift.get().getShiftId()).orElse(null);
            if (shift != null) {
                return Optional.of(toShiftResponse(shift));
            }
        }

        return Optional.empty();
    }

    /**
     * Get current shift for employee (today)
     */
    @Transactional(readOnly = true)
    public Optional<ShiftResponse> getCurrentShiftForEmployee(Long employeeId) {
        return getEmployeeShiftForDate(employeeId, LocalDate.now());
    }

    /**
     * Check if employee should be working on a given date
     */
    @Transactional(readOnly = true)
    public boolean isWorkingDay(Long employeeId, LocalDate date) {
        return getEmployeeShiftForDate(employeeId, date).isPresent();
    }

    /**
     * Calculate if check-in is late based on employee's shift
     */
    public boolean isLateCheckIn(Long employeeId, LocalDate date, LocalTime checkInTime) {
        Optional<ShiftResponse> shift = getEmployeeShiftForDate(employeeId, date);
        if (shift.isEmpty()) {
            return false; // No shift assigned, can't be late
        }

        LocalTime graceLimit = shift.get().getStartTime().plusMinutes(shift.get().getGracePeriodMinutes());
        return checkInTime.isAfter(graceLimit);
    }

    /**
     * Calculate minutes late
     */
    public int getMinutesLate(Long employeeId, LocalDate date, LocalTime checkInTime) {
        Optional<ShiftResponse> shift = getEmployeeShiftForDate(employeeId, date);
        if (shift.isEmpty()) {
            return 0;
        }

        LocalTime graceLimit = shift.get().getStartTime().plusMinutes(shift.get().getGracePeriodMinutes());
        if (checkInTime.isBefore(graceLimit) || checkInTime.equals(graceLimit)) {
            return 0;
        }

        return (int) Duration.between(shift.get().getStartTime(), checkInTime).toMinutes();
    }

    /**
     * Calculate if check-out is early
     */
    public boolean isEarlyCheckOut(Long employeeId, LocalDate date, LocalTime checkOutTime) {
        Optional<ShiftResponse> shift = getEmployeeShiftForDate(employeeId, date);
        if (shift.isEmpty()) {
            return false;
        }

        return checkOutTime.isBefore(shift.get().getEndTime());
    }

    /**
     * Calculate minutes left early
     */
    public int getMinutesEarly(Long employeeId, LocalDate date, LocalTime checkOutTime) {
        Optional<ShiftResponse> shift = getEmployeeShiftForDate(employeeId, date);
        if (shift.isEmpty()) {
            return 0;
        }

        if (checkOutTime.isAfter(shift.get().getEndTime()) || checkOutTime.equals(shift.get().getEndTime())) {
            return 0;
        }

        return (int) Duration.between(checkOutTime, shift.get().getEndTime()).toMinutes();
    }

    // ==================== Helper Methods ====================

    private Shift findShiftByIdAndTenant(Long shiftId) {
        Long tenantId = TenantContext.getTenantId();
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        if (!shift.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Shift not found in your organization");
        }

        return shift;
    }

    private ShiftResponse toShiftResponse(Shift shift) {
        Duration duration = Duration.between(shift.getStartTime(), shift.getEndTime());

        return ShiftResponse.builder()
                .id(shift.getId())
                .tenantId(shift.getTenantId())
                .name(shift.getName())
                .code(shift.getCode())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .gracePeriodMinutes(shift.getGracePeriodMinutes())
                .halfDayHours(shift.getHalfDayHours())
                .fullDayHours(shift.getFullDayHours())
                .color(shift.getColor())
                .description(shift.getDescription())
                .isActive(shift.getIsActive())
                .createdAt(shift.getCreatedAt())
                .updatedAt(shift.getUpdatedAt())
                .graceTimeLimit(shift.getGraceTimeLimit())
                .totalHours((int) duration.toHours())
                .totalMinutes((int) duration.toMinutes())
                .build();
    }

    private EmployeeShiftResponse toEmployeeShiftResponse(EmployeeShift employeeShift, Employee employee, Shift shift) {
        return EmployeeShiftResponse.builder()
                .id(employeeShift.getId())
                .employeeId(employeeShift.getEmployeeId())
                .employeeName(employee != null ? employee.getFirstName() + " " + employee.getLastName() : null)
                .employeeCode(employee != null ? employee.getEmployeeCode() : null)
                .shiftId(employeeShift.getShiftId())
                .shift(shift != null ? toShiftResponse(shift) : null)
                .effectiveFrom(employeeShift.getEffectiveFrom())
                .effectiveTo(employeeShift.getEffectiveTo())
                .daysOfWeek(Arrays.asList(employeeShift.getDaysOfWeek()))
                .isActive(employeeShift.getIsActive())
                .assignedBy(employeeShift.getAssignedBy())
                .notes(employeeShift.getNotes())
                .createdAt(employeeShift.getCreatedAt())
                .updatedAt(employeeShift.getUpdatedAt())
                .isCurrent(employeeShift.isCurrent())
                .isOngoing(employeeShift.isOngoing())
                .isExpired(employeeShift.isExpired())
                .build();
    }

    private PageResponse<ShiftResponse> mapToPageResponse(Page<Shift> page) {
        List<ShiftResponse> content = page.getContent()
                .stream()
                .map(this::toShiftResponse)
                .collect(Collectors.toList());

        return PageResponse.<ShiftResponse>builder()
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
}
