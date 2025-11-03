package com.worksyncx.hrms.entity;

import com.worksyncx.hrms.entity.base.BaseEntity;
import com.worksyncx.hrms.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "payroll_cycles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "month", "year"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PayrollCycle extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "salary_date")
    private LocalDate salaryDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PayrollStatus status = PayrollStatus.DRAFT;
}
