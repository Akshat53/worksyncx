package com.worksyncx.hrms.dto.payroll;

import com.worksyncx.hrms.enums.PayrollStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayrollRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Payroll cycle ID is required")
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
}
