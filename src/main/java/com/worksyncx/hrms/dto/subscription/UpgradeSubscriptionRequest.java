package com.worksyncx.hrms.dto.subscription;

import com.worksyncx.hrms.enums.BillingCycle;
import com.worksyncx.hrms.enums.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpgradeSubscriptionRequest {

    @NotNull(message = "Plan is required")
    private SubscriptionPlan plan;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    // Payment information (will be used with payment gateway)
    private String paymentMethodId; // Stripe payment method ID

    // Optional: Coupon/promo code
    private String couponCode;
}
