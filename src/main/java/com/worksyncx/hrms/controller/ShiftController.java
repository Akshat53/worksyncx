package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.annotation.RequiresModule;
import com.worksyncx.hrms.enums.Module;
import com.worksyncx.hrms.dto.common.PageResponse;
import com.worksyncx.hrms.annotation.RequiresModule;
import com.worksyncx.hrms.enums.Module;
import com.worksyncx.hrms.dto.shift.*;
import com.worksyncx.hrms.service.shift.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    @RequiresModule(Module.SHIFTS)
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER', 'SHIFT:CREATE')")
    public ResponseEntity<ShiftResponse> createShift(@Valid @RequestBody ShiftRequest request) {
        ShiftResponse response = shiftService.createShift(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @RequiresModule(Module.SHIFTS)
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_EMPLOYEE', 'SHIFT:READ')")
    public ResponseEntity<List<ShiftResponse>> getAllShifts() {
        List<ShiftResponse> shifts = shiftService.getAllShifts();
        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/active")
    @RequiresModule(Module.SHIFTS)
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_EMPLOYEE', 'SHIFT:READ')")
    public ResponseEntity<List<ShiftResponse>> getActiveShifts() {
        List<ShiftResponse> shifts = shiftService.getActiveShifts();
        return ResponseEntity.ok(shifts);
    }

    @GetMapping("/{id}")
    @RequiresModule(Module.SHIFTS)
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_EMPLOYEE', 'SHIFT:READ')")
    public ResponseEntity<ShiftResponse> getShiftById(@PathVariable Long id) {
        ShiftResponse shift = shiftService.getShiftById(id);
        return ResponseEntity.ok(shift);
    }

    @PutMapping("/{id}")
    @RequiresModule(Module.SHIFTS)
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER', 'SHIFT:UPDATE')")
    public ResponseEntity<ShiftResponse> updateShift(
            @PathVariable Long id,
            @Valid @RequestBody ShiftRequest request) {
        ShiftResponse response = shiftService.updateShift(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @RequiresModule(Module.SHIFTS)
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER', 'SHIFT:DELETE')")
    public ResponseEntity<Void> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.noContent().build();
    }

    // Employee Shift Assignments
    @PostMapping("/assignments")
    @RequiresModule(Module.SHIFTS)
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER', 'SHIFT:ASSIGN')")
    public ResponseEntity<EmployeeShiftResponse> assignShift(@Valid @RequestBody AssignShiftRequest request) {
        EmployeeShiftResponse response = shiftService.assignShiftToEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/assignments/employee/{employeeId}")
    @RequiresModule(Module.SHIFTS)
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_EMPLOYEE', 'SHIFT:READ')")
    public ResponseEntity<List<EmployeeShiftResponse>> getEmployeeShifts(@PathVariable Long employeeId) {
        List<EmployeeShiftResponse> shifts = shiftService.getEmployeeShifts(employeeId);
        return ResponseEntity.ok(shifts);
    }

    @PutMapping("/assignments/{id}")
    @RequiresModule(Module.SHIFTS)
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER', 'SHIFT:ASSIGN')")
    public ResponseEntity<EmployeeShiftResponse> updateShiftAssignment(
            @PathVariable Long id,
            @Valid @RequestBody AssignShiftRequest request) {
        EmployeeShiftResponse response = shiftService.updateShiftAssignment(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/assignments/{id}")
    @RequiresModule(Module.SHIFTS)
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER', 'SHIFT:ASSIGN')")
    public ResponseEntity<Void> deleteShiftAssignment(@PathVariable Long id) {
        shiftService.deleteShiftAssignment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{shiftId}/employees")
    @RequiresModule(Module.SHIFTS)
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER', 'SHIFT:READ')")
    public ResponseEntity<List<EmployeeShiftResponse>> getEmployeesOnShift(
            @PathVariable Long shiftId,
            @RequestParam(required = false) LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        List<EmployeeShiftResponse> employees = shiftService.getEmployeesOnShift(shiftId, queryDate);
        return ResponseEntity.ok(employees);
    }

    // Shift detection endpoints for attendance system
    @GetMapping("/employee/{employeeId}/current")
    @RequiresModule(Module.SHIFTS)
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_EMPLOYEE', 'SHIFT:READ')")
    public ResponseEntity<ShiftResponse> getCurrentShift(@PathVariable Long employeeId) {
        return shiftService.getCurrentShiftForEmployee(employeeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}/date/{date}")
    @RequiresModule(Module.SHIFTS)
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_EMPLOYEE', 'SHIFT:READ')")
    public ResponseEntity<ShiftResponse> getShiftForDate(
            @PathVariable Long employeeId,
            @PathVariable LocalDate date) {
        return shiftService.getEmployeeShiftForDate(employeeId, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/page")
    @RequiresModule(Module.SHIFTS)
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_EMPLOYEE', 'SHIFT:READ')")
    public ResponseEntity<PageResponse<ShiftResponse>> getAllShiftsPaginated(
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        // Create sort object
        Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<ShiftResponse> shiftsPage;

        if (active != null) {
            shiftsPage = shiftService.getActiveShiftsPaginated(pageable);
        } else {
            shiftsPage = shiftService.getAllShiftsPaginated(pageable);
        }

        return ResponseEntity.ok(shiftsPage);
    }
}
