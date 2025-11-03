package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.employee.EmployeeRequest;
import com.worksyncx.hrms.dto.employee.EmployeeResponse;
import com.worksyncx.hrms.enums.EmploymentStatus;
import com.worksyncx.hrms.service.employee.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
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
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok(Map.of("message", "Employee deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to delete employee", "message", e.getMessage()));
        }
    }
}
