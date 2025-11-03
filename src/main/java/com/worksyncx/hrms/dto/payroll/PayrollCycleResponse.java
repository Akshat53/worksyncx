package com.worksyncx.hrms.dto.payroll;

import com.worksyncx.hrms.enums.PayrollStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollCycleResponse {
    private Long id;
    private Long tenantId;
    private String name;
    private Integer month;
    private Integer year;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate salaryDate;
    private PayrollStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
