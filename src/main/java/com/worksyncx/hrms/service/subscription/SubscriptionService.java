package com.worksyncx.hrms.service.subscription;

import com.worksyncx.hrms.dto.subscription.ModuleAccessResponse;
import com.worksyncx.hrms.dto.subscription.PlanDetailsResponse;
import com.worksyncx.hrms.dto.subscription.SubscriptionResponse;
import com.worksyncx.hrms.entity.Subscription;
import com.worksyncx.hrms.enums.SubscriptionPlan;
import com.worksyncx.hrms.enums.SubscriptionStatus;
import com.worksyncx.hrms.repository.EmployeeRepository;
import com.worksyncx.hrms.repository.SubscriptionRepository;
import com.worksyncx.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrentSubscription(Long tenantId) {
        Subscription subscription = subscriptionRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new RuntimeException("No subscription found for tenant"));

        int currentEmployees = (int) employeeRepository.countByTenantId(tenantId);

        return SubscriptionResponse.builder()
            .id(subscription.getId())
            .tenantId(tenantId)
            .plan(subscription.getPlan())
            .status(subscription.getStatus())
            .startDate(subscription.getStartDate())
            .endDate(subscription.getEndDate())
            .maxEmployees(subscription.getMaxEmployees())
            .currentEmployees(currentEmployees)
            .modules(subscription.getModules())
            .features(subscription.getFeatures())
            .billingCycle(subscription.getBillingCycle())
            .amount(subscription.getAmount())
            .autoRenewal(subscription.getAutoRenewal())
            .build();
    }

    @Transactional(readOnly = true)
    public boolean hasModuleAccess(Long tenantId, String module) {
        Subscription subscription = subscriptionRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new RuntimeException("No subscription found for tenant"));

        // Check if subscription is active
        if (!SubscriptionStatus.ACTIVE.equals(subscription.getStatus())) {
            return false;
        }

        // Check expiration (null endDate means no expiration - for FREE plan)
        if (subscription.getEndDate() != null &&
            java.time.LocalDate.now().isAfter(subscription.getEndDate())) {
            // Mark as expired
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            return false;
        }

        // Case-insensitive check for backwards compatibility with old data
        return subscription.getModules().stream()
            .anyMatch(m -> m.equalsIgnoreCase(module));
    }

    @Transactional(readOnly = true)
    public Set<String> getAvailableModules(Long tenantId) {
        Subscription subscription = subscriptionRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new RuntimeException("No subscription found for tenant"));

        return subscription.getModules();
    }

    @Transactional(readOnly = true)
    public boolean checkEmployeeLimit(Long tenantId) {
        Subscription subscription = subscriptionRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new RuntimeException("No subscription found for tenant"));

        int currentEmployees = (int) employeeRepository.countByTenantId(tenantId);
        return currentEmployees < subscription.getMaxEmployees();
    }

    @Transactional(readOnly = true)
    public ModuleAccessResponse checkModuleAccess(Long tenantId, String module) {
        Subscription subscription = subscriptionRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new RuntimeException("No subscription found for tenant"));

        boolean hasAccess = subscription.getModules().contains(module);
        String requiredPlan = getRequiredPlanForModule(module);

        return ModuleAccessResponse.builder()
            .module(module)
            .hasAccess(hasAccess)
            .requiredPlan(hasAccess ? null : requiredPlan)
            .build();
    }

    public List<PlanDetailsResponse> getAllPlans() {
        return Arrays.asList(
            PlanDetailsResponse.builder()
                .plan(SubscriptionPlan.FREE)
                .name("Free")
                .description("Perfect for getting started")
                .monthlyPrice(BigDecimal.ZERO)
                .yearlyPrice(BigDecimal.ZERO)
                .maxEmployees(5)
                .modules(Set.of("DEPARTMENTS", "DESIGNATIONS", "EMPLOYEES"))
                .features(Set.of("Basic employee management", "Department structure", "Designation management"))
                .isPopular(false)
                .build(),
            PlanDetailsResponse.builder()
                .plan(SubscriptionPlan.STARTER)
                .name("Starter")
                .description("For small teams")
                .monthlyPrice(new BigDecimal("29.00"))
                .yearlyPrice(new BigDecimal("290.00"))
                .maxEmployees(50)
                .modules(Set.of("DEPARTMENTS", "DESIGNATIONS", "EMPLOYEES", "ATTENDANCE", "LEAVE_MANAGEMENT", "SHIFTS"))
                .features(Set.of("Everything in Free", "Time tracking", "Attendance management", "Leave approvals", "Shift management", "Basic reports"))
                .isPopular(false)
                .build(),
            PlanDetailsResponse.builder()
                .plan(SubscriptionPlan.PROFESSIONAL)
                .name("Professional")
                .description("For growing businesses")
                .monthlyPrice(new BigDecimal("79.00"))
                .yearlyPrice(new BigDecimal("790.00"))
                .maxEmployees(200)
                .modules(Set.of("DEPARTMENTS", "DESIGNATIONS", "EMPLOYEES", "ATTENDANCE", "LEAVE_MANAGEMENT", "PAYROLL", "SHIFTS"))
                .features(Set.of("Everything in Starter", "Payroll processing", "Salary management", "Advanced reports", "Priority support"))
                .isPopular(true)
                .build(),
            PlanDetailsResponse.builder()
                .plan(SubscriptionPlan.ENTERPRISE)
                .name("Enterprise")
                .description("For large organizations")
                .monthlyPrice(null)
                .yearlyPrice(null)
                .maxEmployees(null)
                .modules(Set.of("DEPARTMENTS", "DESIGNATIONS", "EMPLOYEES", "ATTENDANCE", "LEAVE_MANAGEMENT", "PAYROLL", "SHIFTS", "REPORTS"))
                .features(Set.of("Everything in Professional", "Custom modules", "SSO integration", "Dedicated support", "Custom integrations", "SLA guarantee"))
                .isPopular(false)
                .build()
        );
    }

    private String getRequiredPlanForModule(String module) {
        return switch (module) {
            case "DEPARTMENTS", "DESIGNATIONS", "EMPLOYEES" -> "FREE";
            case "ATTENDANCE", "LEAVE_MANAGEMENT" -> "STARTER";
            case "PAYROLL" -> "PROFESSIONAL";
            case "REPORTS" -> "ENTERPRISE";
            default -> "ENTERPRISE";
        };
    }

    @Transactional
    public SubscriptionResponse upgradeSubscription(Long tenantId, SubscriptionPlan newPlan, String billingCycle) {
        Subscription subscription = subscriptionRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new RuntimeException("No subscription found for tenant"));

        // Validate upgrade path
        if (isDowngrade(subscription.getPlan(), newPlan)) {
            throw new RuntimeException("Please use downgrade endpoint to switch to a lower plan");
        }

        // Update subscription details
        subscription.setPlan(newPlan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(LocalDate.now());

        // Set modules and limits based on plan
        updateSubscriptionByPlan(subscription, newPlan, billingCycle);

        subscription = subscriptionRepository.save(subscription);

        int currentEmployees = employeeRepository.findByTenantId(tenantId).size();

        return SubscriptionResponse.builder()
            .id(subscription.getId())
            .tenantId(tenantId)
            .plan(subscription.getPlan())
            .status(subscription.getStatus())
            .startDate(subscription.getStartDate())
            .endDate(subscription.getEndDate())
            .maxEmployees(subscription.getMaxEmployees())
            .currentEmployees(currentEmployees)
            .modules(subscription.getModules())
            .features(subscription.getFeatures())
            .billingCycle(subscription.getBillingCycle())
            .amount(subscription.getAmount())
            .autoRenewal(subscription.getAutoRenewal())
            .build();
    }

    private void updateSubscriptionByPlan(Subscription subscription, SubscriptionPlan plan, String billingCycle) {
        switch (plan) {
            case FREE -> {
                subscription.setMaxEmployees(5);
                subscription.setModules(Set.of("DEPARTMENTS", "DESIGNATIONS", "EMPLOYEES"));
                subscription.setFeatures(Set.of("Basic employee management"));
                subscription.setAmount(BigDecimal.ZERO);
                subscription.setEndDate(null); // No expiry
            }
            case STARTER -> {
                subscription.setMaxEmployees(50);
                subscription.setModules(Set.of("DEPARTMENTS", "DESIGNATIONS", "EMPLOYEES", "ATTENDANCE", "LEAVE_MANAGEMENT", "SHIFTS"));
                subscription.setFeatures(Set.of("Everything in Free", "Time tracking", "Leave management", "Shift management"));
                subscription.setBillingCycle(com.worksyncx.hrms.enums.BillingCycle.valueOf(billingCycle));
                subscription.setAmount("MONTHLY".equals(billingCycle) ? new BigDecimal("29.00") : new BigDecimal("290.00"));
                subscription.setEndDate(LocalDate.now().plusMonths("MONTHLY".equals(billingCycle) ? 1 : 12));
            }
            case PROFESSIONAL -> {
                subscription.setMaxEmployees(200);
                subscription.setModules(Set.of("DEPARTMENTS", "DESIGNATIONS", "EMPLOYEES", "ATTENDANCE", "LEAVE_MANAGEMENT", "PAYROLL", "SHIFTS"));
                subscription.setFeatures(Set.of("Everything in Starter", "Payroll processing", "Advanced reports"));
                subscription.setBillingCycle(com.worksyncx.hrms.enums.BillingCycle.valueOf(billingCycle));
                subscription.setAmount("MONTHLY".equals(billingCycle) ? new BigDecimal("79.00") : new BigDecimal("790.00"));
                subscription.setEndDate(LocalDate.now().plusMonths("MONTHLY".equals(billingCycle) ? 1 : 12));
            }
            case ENTERPRISE -> {
                subscription.setMaxEmployees(999999); // Unlimited
                subscription.setModules(Set.of("DEPARTMENTS", "DESIGNATIONS", "EMPLOYEES", "ATTENDANCE", "LEAVE_MANAGEMENT", "PAYROLL", "SHIFTS", "REPORTS"));
                subscription.setFeatures(Set.of("Everything in Professional", "Custom modules", "Dedicated support"));
                subscription.setBillingCycle(com.worksyncx.hrms.enums.BillingCycle.ANNUAL);
                subscription.setAmount(BigDecimal.ZERO); // Contact sales
                subscription.setEndDate(LocalDate.now().plusYears(1));
            }
        }
    }

    private boolean isDowngrade(SubscriptionPlan current, SubscriptionPlan target) {
        int currentLevel = getPlanLevel(current);
        int targetLevel = getPlanLevel(target);
        return targetLevel < currentLevel;
    }

    private int getPlanLevel(SubscriptionPlan plan) {
        return switch (plan) {
            case FREE -> 0;
            case STARTER -> 1;
            case PROFESSIONAL -> 2;
            case ENTERPRISE -> 3;
        };
    }
}
