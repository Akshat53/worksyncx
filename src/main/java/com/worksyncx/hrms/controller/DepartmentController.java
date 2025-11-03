package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.department.DepartmentRequest;
import com.worksyncx.hrms.dto.department.DepartmentResponse;
import com.worksyncx.hrms.service.department.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<?> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        try {
            DepartmentResponse response = departmentService.createDepartment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to create department", "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments(
        @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        List<DepartmentResponse> departments = activeOnly
            ? departmentService.getActiveDepartments()
            : departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDepartmentById(@PathVariable Long id) {
        try {
            DepartmentResponse response = departmentService.getDepartmentById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Department not found", "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(
        @PathVariable Long id,
        @Valid @RequestBody DepartmentRequest request
    ) {
        try {
            DepartmentResponse response = departmentService.updateDepartment(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to update department", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.ok(Map.of("message", "Department deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to delete department", "message", e.getMessage()));
        }
    }
}
