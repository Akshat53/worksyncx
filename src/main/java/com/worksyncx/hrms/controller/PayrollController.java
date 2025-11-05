package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.annotation.RequiresModule;
import com.worksyncx.hrms.dto.payroll.PayrollCycleRequest;
import com.worksyncx.hrms.dto.payroll.PayrollCycleResponse;
import com.worksyncx.hrms.dto.payroll.PayrollRequest;
import com.worksyncx.hrms.dto.payroll.PayrollResponse;
import com.worksyncx.hrms.enums.Module;
import com.worksyncx.hrms.service.payroll.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    // ==================== Payroll Cycle Endpoints ====================

    @PostMapping("/cycles")
    @RequiresModule(Module.PAYROLL)
    public ResponseEntity<?> createPayrollCycle(@Valid @RequestBody PayrollCycleRequest request) {
        try {
            PayrollCycleResponse response = payrollService.createPayrollCycle(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to create payroll cycle", "message", e.getMessage()));
        }
    }

    @GetMapping("/cycles")
    @RequiresModule(Module.PAYROLL)
    public ResponseEntity<List<PayrollCycleResponse>> getAllPayrollCycles() {
        List<PayrollCycleResponse> cycles = payrollService.getAllPayrollCycles();
        return ResponseEntity.ok(cycles);
    }

    @GetMapping("/cycles/{id}")
    @RequiresModule(Module.PAYROLL)
    public ResponseEntity<?> getPayrollCycleById(@PathVariable Long id) {
        try {
            PayrollCycleResponse response = payrollService.getPayrollCycleById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Payroll cycle not found", "message", e.getMessage()));
        }
    }

    @PutMapping("/cycles/{id}")
    @RequiresModule(Module.PAYROLL)
    public ResponseEntity<?> updatePayrollCycle(
        @PathVariable Long id,
        @Valid @RequestBody PayrollCycleRequest request
    ) {
        try {
            PayrollCycleResponse response = payrollService.updatePayrollCycle(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to update payroll cycle", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/cycles/{id}")
    @RequiresModule(Module.PAYROLL)
    public ResponseEntity<?> deletePayrollCycle(@PathVariable Long id) {
        try {
            payrollService.deletePayrollCycle(id);
            return ResponseEntity.ok(Map.of("message", "Payroll cycle deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to delete payroll cycle", "message", e.getMessage()));
        }
    }

    // ==================== Payroll Endpoints ====================

    @PostMapping
    @RequiresModule(Module.PAYROLL)
    public ResponseEntity<?> createPayroll(@Valid @RequestBody PayrollRequest request) {
        try {
            PayrollResponse response = payrollService.createPayroll(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to create payroll", "message", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_TENANT_ADMIN', 'ROLE_EMPLOYEE')")
    public ResponseEntity<?> getAllPayrolls(
        @RequestParam(required = false) Long cycleId,
        @RequestParam(required = false) Long employeeId
    ) {
        try {
            List<PayrollResponse> payrolls;

            if (cycleId != null) {
                payrolls = payrollService.getPayrollsByCycle(cycleId);
            } else if (employeeId != null) {
                payrolls = payrollService.getPayrollsByEmployee(employeeId);
            } else {
                payrolls = payrollService.getAllPayrolls();
            }

            return ResponseEntity.ok(payrolls);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to get payrolls", "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @RequiresModule(Module.PAYROLL)
    public ResponseEntity<?> getPayrollById(@PathVariable Long id) {
        try {
            PayrollResponse response = payrollService.getPayrollById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Payroll not found", "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequiresModule(Module.PAYROLL)
    public ResponseEntity<?> updatePayroll(
        @PathVariable Long id,
        @Valid @RequestBody PayrollRequest request
    ) {
        try {
            PayrollResponse response = payrollService.updatePayroll(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to update payroll", "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/mark-paid")
    @RequiresModule(Module.PAYROLL)
    public ResponseEntity<?> markAsPaid(
        @PathVariable Long id,
        @RequestBody(required = false) Map<String, String> body
    ) {
        try {
            String bankTransferRef = body != null ? body.get("bankTransferRef") : null;
            PayrollResponse response = payrollService.markAsPaid(id, bankTransferRef);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to mark payroll as paid", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequiresModule(Module.PAYROLL)
    public ResponseEntity<?> deletePayroll(@PathVariable Long id) {
        try {
            payrollService.deletePayroll(id);
            return ResponseEntity.ok(Map.of("message", "Payroll deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to delete payroll", "message", e.getMessage()));
        }
    }
}
