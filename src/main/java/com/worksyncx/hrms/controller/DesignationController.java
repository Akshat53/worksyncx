package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.common.PageResponse;
import com.worksyncx.hrms.dto.designation.DesignationRequest;
import com.worksyncx.hrms.dto.designation.DesignationResponse;
import com.worksyncx.hrms.service.designation.DesignationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('DESIGNATION:CREATE')")
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
    @PreAuthorize("hasAuthority('DESIGNATION:READ')")
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

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('DESIGNATION:READ')")
    public ResponseEntity<PageResponse<DesignationResponse>> getAllDesignationsPaginated(
        @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
        @RequestParam(required = false) Long departmentId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        // Create sort object
        Sort sort = sortDirection.equalsIgnoreCase("DESC")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<DesignationResponse> designationsPage;
        if (departmentId != null) {
            designationsPage = designationService.getDesignationsByDepartmentPaginated(departmentId, pageable);
        } else if (activeOnly) {
            designationsPage = designationService.getActiveDesignationsPaginated(pageable);
        } else {
            designationsPage = designationService.getAllDesignationsPaginated(pageable);
        }

        return ResponseEntity.ok(designationsPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DESIGNATION:READ')")
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
    @PreAuthorize("hasAuthority('DESIGNATION:UPDATE')")
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
    @PreAuthorize("hasAuthority('DESIGNATION:DELETE')")
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
