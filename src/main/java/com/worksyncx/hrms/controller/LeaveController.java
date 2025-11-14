package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.annotation.RequiresModule;
import com.worksyncx.hrms.dto.common.PageResponse;
import com.worksyncx.hrms.dto.leave.*;
import com.worksyncx.hrms.enums.LeaveStatus;
import com.worksyncx.hrms.enums.Module;
import com.worksyncx.hrms.service.leave.LeaveService;
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
@RequestMapping("/api/leave")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    // ==================== Leave Type Endpoints ====================

    @PostMapping("/types")
    @RequiresModule(Module.LEAVE_MANAGEMENT)
    @PreAuthorize("hasAuthority('LEAVE:CREATE')")
    public ResponseEntity<?> createLeaveType(@Valid @RequestBody LeaveTypeRequest request) {
        try {
            LeaveTypeResponse response = leaveService.createLeaveType(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to create leave type", "message", e.getMessage()));
        }
    }

    @GetMapping("/types")
    @RequiresModule(Module.LEAVE_MANAGEMENT)
    @PreAuthorize("hasAuthority('LEAVE:READ')")
    public ResponseEntity<List<LeaveTypeResponse>> getAllLeaveTypes(
        @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        List<LeaveTypeResponse> leaveTypes = activeOnly
            ? leaveService.getActiveLeaveTypes()
            : leaveService.getAllLeaveTypes();
        return ResponseEntity.ok(leaveTypes);
    }

    @GetMapping("/types/{id}")
    @RequiresModule(Module.LEAVE_MANAGEMENT)
    @PreAuthorize("hasAuthority('LEAVE:READ')")
    public ResponseEntity<?> getLeaveTypeById(@PathVariable Long id) {
        try {
            LeaveTypeResponse response = leaveService.getLeaveTypeById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Leave type not found", "message", e.getMessage()));
        }
    }

    @PutMapping("/types/{id}")
    @RequiresModule(Module.LEAVE_MANAGEMENT)
    @PreAuthorize("hasAuthority('LEAVE:UPDATE')")
    public ResponseEntity<?> updateLeaveType(
        @PathVariable Long id,
        @Valid @RequestBody LeaveTypeRequest request
    ) {
        try {
            LeaveTypeResponse response = leaveService.updateLeaveType(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to update leave type", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/types/{id}")
    @RequiresModule(Module.LEAVE_MANAGEMENT)
    @PreAuthorize("hasAuthority('LEAVE:DELETE')")
    public ResponseEntity<?> deleteLeaveType(@PathVariable Long id) {
        try {
            leaveService.deleteLeaveType(id);
            return ResponseEntity.ok(Map.of("message", "Leave type deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to delete leave type", "message", e.getMessage()));
        }
    }

    // ==================== Leave Request Endpoints ====================

    @PostMapping("/requests")
    @RequiresModule(Module.LEAVE_MANAGEMENT)
    @PreAuthorize("hasAnyAuthority('LEAVE:CREATE', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> createLeaveRequest(@Valid @RequestBody LeaveRequestDto request) {
        try {
            LeaveRequestResponse response = leaveService.createLeaveRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to create leave request", "message", e.getMessage()));
        }
    }

    @GetMapping("/requests")
    @RequiresModule(Module.LEAVE_MANAGEMENT)
    @PreAuthorize("hasAnyAuthority('LEAVE:READ', 'ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<?> getAllLeaveRequests(
        @RequestParam(required = false) Long employeeId,
        @RequestParam(required = false) String status
    ) {
        try {
            List<LeaveRequestResponse> requests;

            if (employeeId != null) {
                requests = leaveService.getLeaveRequestsByEmployee(employeeId);
            } else if (status != null) {
                LeaveStatus leaveStatus = LeaveStatus.valueOf(status.toUpperCase());
                requests = leaveService.getLeaveRequestsByStatus(leaveStatus);
            } else {
                requests = leaveService.getAllLeaveRequests();
            }

            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid status value", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to get leave requests", "message", e.getMessage()));
        }
    }

    @GetMapping("/requests/page")
    @RequiresModule(Module.LEAVE_MANAGEMENT)
    @PreAuthorize("hasAnyAuthority('LEAVE:READ', 'ROLE_TENANT_ADMIN', 'ROLE_HR_MANAGER')")
    public ResponseEntity<?> getAllLeaveRequestsPaginated(
        @RequestParam(required = false) Long employeeId,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        try {
            // Create sort object
            Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, sort);

            PageResponse<LeaveRequestResponse> requestsPage;

            if (employeeId != null) {
                requestsPage = leaveService.getLeaveRequestsByEmployeePaginated(employeeId, pageable);
            } else if (status != null) {
                LeaveStatus leaveStatus = LeaveStatus.valueOf(status.toUpperCase());
                requestsPage = leaveService.getLeaveRequestsByStatusPaginated(leaveStatus, pageable);
            } else {
                requestsPage = leaveService.getAllLeaveRequestsPaginated(pageable);
            }

            return ResponseEntity.ok(requestsPage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid status value", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to get leave requests", "message", e.getMessage()));
        }
    }

    @GetMapping("/requests/{id}")
    @RequiresModule(Module.LEAVE_MANAGEMENT)
    @PreAuthorize("hasAnyAuthority('LEAVE:READ', 'ROLE_TENANT_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> getLeaveRequestById(@PathVariable Long id) {
        try {
            LeaveRequestResponse response = leaveService.getLeaveRequestById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Leave request not found", "message", e.getMessage()));
        }
    }

    @PostMapping("/requests/{id}/approve")
    @RequiresModule(Module.LEAVE_MANAGEMENT)
    @PreAuthorize("hasAuthority('LEAVE:APPROVE')")
    public ResponseEntity<?> approveLeaveRequest(
        @PathVariable Long id,
        @RequestBody LeaveApprovalRequest request
    ) {
        try {
            LeaveRequestResponse response = leaveService.approveLeaveRequest(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to approve leave request", "message", e.getMessage()));
        }
    }

    @PostMapping("/requests/{id}/reject")
    @RequiresModule(Module.LEAVE_MANAGEMENT)
    @PreAuthorize("hasAuthority('LEAVE:REJECT')")
    public ResponseEntity<?> rejectLeaveRequest(
        @PathVariable Long id,
        @Valid @RequestBody LeaveRejectionRequest request
    ) {
        try {
            LeaveRequestResponse response = leaveService.rejectLeaveRequest(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to reject leave request", "message", e.getMessage()));
        }
    }

    @PostMapping("/requests/{id}/cancel")
    @RequiresModule(Module.LEAVE_MANAGEMENT)
    @PreAuthorize("hasAnyAuthority('LEAVE:UPDATE', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> cancelLeaveRequest(@PathVariable Long id) {
        try {
            LeaveRequestResponse response = leaveService.cancelLeaveRequest(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to cancel leave request", "message", e.getMessage()));
        }
    }
}
