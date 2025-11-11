package com.worksyncx.hrms.dto.subscription;

import com.worksyncx.hrms.enums.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDetailsResponse {
    private SubscriptionPlan plan;
    private String name;
    private String description;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    private Integer maxEmployees;
    private Set<String> modules;
    private Set<String> features;
    private Boolean isPopular;
}
