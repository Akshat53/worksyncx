package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.designation.DesignationRequest;
import com.worksyncx.hrms.dto.designation.DesignationResponse;
import com.worksyncx.hrms.service.designation.DesignationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/designations")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService designationService;

    @PostMapping
    public ResponseEntity<?> createDesignation(@Valid @RequestBody DesignationRequest request) {
        try {
            DesignationResponse response = designationService.createDesignation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to create designation", "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<DesignationResponse>> getAllDesignations(
        @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
        @RequestParam(required = false) Long departmentId
    ) {
        List<DesignationResponse> designations;

        if (departmentId != null) {
            designations = designationService.getDesignationsByDepartment(departmentId);
        } else if (activeOnly) {
            designations = designationService.getActiveDesignations();
        } else {
            designations = designationService.getAllDesignations();
        }

        return ResponseEntity.ok(designations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDesignationById(@PathVariable Long id) {
        try {
            DesignationResponse response = designationService.getDesignationById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Designation not found", "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDesignation(
        @PathVariable Long id,
        @Valid @RequestBody DesignationRequest request
    ) {
        try {
            DesignationResponse response = designationService.updateDesignation(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to update designation", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDesignation(@PathVariable Long id) {
        try {
            designationService.deleteDesignation(id);
            return ResponseEntity.ok(Map.of("message", "Designation deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to delete designation", "message", e.getMessage()));
        }
    }
}
