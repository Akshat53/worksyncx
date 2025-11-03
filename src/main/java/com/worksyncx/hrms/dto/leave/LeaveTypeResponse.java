package com.worksyncx.hrms.dto.leave;

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
public class LeaveTypeResponse {
    private Long id;
    private Long tenantId;
    private String name;
    private String code;
    private BigDecimal daysPerYear;
    private Boolean isPaid;
    private Boolean requiresApproval;
    private String colorCode;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
