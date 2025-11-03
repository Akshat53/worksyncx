package com.worksyncx.hrms.dto.leave;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LeaveTypeRequest {

    @NotBlank(message = "Leave type name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Leave type code is required")
    @Size(max = 50, message = "Code cannot exceed 50 characters")
    private String code;

    private BigDecimal daysPerYear;

    private Boolean isPaid = true;

    private Boolean requiresApproval = true;

    @Size(max = 10, message = "Color code cannot exceed 10 characters")
    private String colorCode;

    private Boolean isActive = true;
}
