package com.worksyncx.hrms.dto.designation;

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
public class DesignationResponse {
    private Long id;
    private Long tenantId;
    private String name;
    private String code;
    private String description;
    private Long departmentId;
    private BigDecimal salaryRangeMin;
    private BigDecimal salaryRangeMax;
    private String level;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
