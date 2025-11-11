package com.worksyncx.hrms.dto.subscription;

import com.worksyncx.hrms.enums.BillingCycle;
import com.worksyncx.hrms.enums.SubscriptionPlan;
import com.worksyncx.hrms.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private Long tenantId;
    private SubscriptionPlan plan;
    private SubscriptionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxEmployees;
    private Integer currentEmployees;
    private Set<String> modules;
    private Set<String> features;
    private BillingCycle billingCycle;
    private BigDecimal amount;
    private Boolean autoRenewal;
}
