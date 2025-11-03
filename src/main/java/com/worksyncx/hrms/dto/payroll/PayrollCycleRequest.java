package com.worksyncx.hrms.dto.payroll;

import com.worksyncx.hrms.enums.PayrollStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PayrollCycleRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Month is required")
    private Integer month;

    @NotNull(message = "Year is required")
    private Integer year;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate salaryDate;

    private PayrollStatus status;
}
