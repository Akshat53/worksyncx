package com.worksyncx.hrms.entity;

import com.worksyncx.hrms.entity.base.BaseEntity;
import com.worksyncx.hrms.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payrolls", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "employee_id", "payroll_cycle_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Payroll extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "payroll_cycle_id", nullable = false)
    private Long payrollCycleId;

    @Column(name = "basic_salary", precision = 12, scale = 2)
    private BigDecimal basicSalary;

    @Column(precision = 12, scale = 2)
    private BigDecimal hra;

    @Column(name = "dearness_allowance", precision = 12, scale = 2)
    private BigDecimal dearnessAllowance;

    @Column(name = "other_allowances", precision = 12, scale = 2)
    private BigDecimal otherAllowances;

    @Column(name = "gross_salary", precision = 12, scale = 2)
    private BigDecimal grossSalary;

    @Column(name = "income_tax", precision = 12, scale = 2)
    private BigDecimal incomeTax;

    @Column(name = "professional_tax", precision = 12, scale = 2)
    private BigDecimal professionalTax;

    @Column(name = "employee_pf", precision = 12, scale = 2)
    private BigDecimal employeePf;

    @Column(name = "employee_esi", precision = 12, scale = 2)
    private BigDecimal employeeEsi;

    @Column(name = "other_deductions", precision = 12, scale = 2)
    private BigDecimal otherDeductions;

    @Column(name = "total_deductions", precision = 12, scale = 2)
    private BigDecimal totalDeductions;

    @Column(name = "net_salary", precision = 12, scale = 2)
    private BigDecimal netSalary;

    @Column(name = "bank_transfer_ref", length = 100)
    private String bankTransferRef;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PayrollStatus status = PayrollStatus.DRAFT;

    @Column(name = "paid_date")
    private LocalDateTime paidDate;
}
