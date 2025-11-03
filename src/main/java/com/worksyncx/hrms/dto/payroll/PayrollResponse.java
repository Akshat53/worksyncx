package com.worksyncx.hrms.dto.payroll;

import com.worksyncx.hrms.enums.PayrollStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollResponse {
    private Long id;
    private Long tenantId;
    private Long employeeId;
    private Long payrollCycleId;
    private BigDecimal basicSalary;
    private BigDecimal hra;
    private BigDecimal dearnessAllowance;
    private BigDecimal otherAllowances;
    private BigDecimal grossSalary;
    private BigDecimal incomeTax;
    private BigDecimal professionalTax;
    private BigDecimal employeePf;
    private BigDecimal employeeEsi;
    private BigDecimal otherDeductions;
    private BigDecimal totalDeductions;
    private BigDecimal netSalary;
    private String bankTransferRef;
    private PayrollStatus status;
    private LocalDateTime paidDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
