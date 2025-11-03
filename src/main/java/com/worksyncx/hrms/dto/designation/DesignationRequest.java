package com.worksyncx.hrms.dto.designation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DesignationRequest {

    @NotBlank(message = "Designation name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Designation code is required")
    @Size(max = 50, message = "Code cannot exceed 50 characters")
    private String code;

    private String description;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    private BigDecimal salaryRangeMin;

    private BigDecimal salaryRangeMax;

    @Size(max = 50, message = "Level cannot exceed 50 characters")
    private String level;

    private Boolean isActive = true;
}
