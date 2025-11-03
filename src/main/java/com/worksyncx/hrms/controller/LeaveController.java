package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.leave.*;
import com.worksyncx.hrms.enums.LeaveStatus;
import com.worksyncx.hrms.service.leave.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<LeaveTypeResponse>> getAllLeaveTypes(
        @RequestParam(required = false, defaultValue = "false") boolean activeOnly
    ) {
        List<LeaveTypeResponse> leaveTypes = activeOnly
            ? leaveService.getActiveLeaveTypes()
            : leaveService.getAllLeaveTypes();
        return ResponseEntity.ok(leaveTypes);
    }

    @GetMapping("/types/{id}")
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

    @GetMapping("/requests/{id}")
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
