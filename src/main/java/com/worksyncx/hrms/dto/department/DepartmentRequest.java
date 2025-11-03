package com.worksyncx.hrms.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Department code is required")
    @Size(max = 50, message = "Code cannot exceed 50 characters")
    private String code;

    private String description;

    private Long headId;

    private Long parentDepartmentId;

    private Boolean isActive = true;
}
