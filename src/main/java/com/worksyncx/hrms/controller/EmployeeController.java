package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.employee.EmployeeRequest;
import com.worksyncx.hrms.dto.employee.EmployeeResponse;
import com.worksyncx.hrms.entity.User;
import com.worksyncx.hrms.enums.EmploymentStatus;
import com.worksyncx.hrms.service.employee.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // Admin-only endpoints
    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE:CREATE')")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        try {
            EmployeeResponse response = employeeService.createEmployee(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to create employee", "message", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('EMPLOYEE:READ')")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long departmentId
    ) {
        List<EmployeeResponse> employees;

        if (departmentId != null) {
            employees = employeeService.getEmployeesByDepartment(departmentId);
        } else if (status != null) {
            try {
                EmploymentStatus employmentStatus = EmploymentStatus.valueOf(status.toUpperCase());
                employees = employeeService.getEmployeesByStatus(employmentStatus);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            employees = employeeService.getAllEmployees();
        }

        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:READ')")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        try {
            EmployeeResponse response = employeeService.getEmployeeById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Employee not found", "message", e.getMessage()));
        }
    }

    @GetMapping("/code/{employeeCode}")
    @PreAuthorize("hasAuthority('EMPLOYEE:READ')")
    public ResponseEntity<?> getEmployeeByCode(@PathVariable String employeeCode) {
        try {
            EmployeeResponse response = employeeService.getEmployeeByCode(employeeCode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Employee not found", "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:UPDATE')")
    public ResponseEntity<?> updateEmployee(
        @PathVariable Long id,
        @Valid @RequestBody EmployeeRequest request
    ) {
        try {
            EmployeeResponse response = employeeService.updateEmployee(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to update employee", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:DELETE')")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok(Map.of("message", "Employee deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to delete employee", "message", e.getMessage()));
        }
    }

    // Employee self-service endpoints
    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> getMyProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            EmployeeResponse response = employeeService.getEmployeeByUserId(user.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Profile not found", "message", e.getMessage()));
        }
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody Map<String, Object> updates) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            EmployeeResponse response = employeeService.updateEmployeeProfile(user.getId(), updates);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to update profile", "message", e.getMessage()));
        }
    }
}
