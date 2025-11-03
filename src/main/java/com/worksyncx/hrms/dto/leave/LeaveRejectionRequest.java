package com.worksyncx.hrms.dto.leave;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LeaveRejectionRequest {
    @NotBlank(message = "Rejection reason is required")
    private String rejectionReason;
}
