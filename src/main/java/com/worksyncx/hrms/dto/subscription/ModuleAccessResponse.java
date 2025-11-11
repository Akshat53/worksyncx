package com.worksyncx.hrms.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleAccessResponse {
    private String module;
    private Boolean hasAccess;
    private String requiredPlan;
}
