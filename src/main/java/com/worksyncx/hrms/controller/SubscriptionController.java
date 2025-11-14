package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.subscription.ModuleAccessResponse;
import com.worksyncx.hrms.dto.subscription.PlanDetailsResponse;
import com.worksyncx.hrms.dto.subscription.SubscriptionResponse;
import com.worksyncx.hrms.dto.subscription.UpgradeSubscriptionRequest;
import com.worksyncx.hrms.security.TenantContext;
import com.worksyncx.hrms.service.subscription.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionResponse> getCurrentSubscription() {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(subscriptionService.getCurrentSubscription(tenantId));
    }

    @GetMapping("/modules")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Set<String>> getAccessibleModules() {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(subscriptionService.getAvailableModules(tenantId));
    }

    @GetMapping("/check-access/{module}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ModuleAccessResponse> checkModuleAccess(@PathVariable String module) {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(subscriptionService.checkModuleAccess(tenantId, module));
    }

    @GetMapping("/employee-limit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> checkEmployeeLimit() {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(subscriptionService.checkEmployeeLimit(tenantId));
    }

    @GetMapping("/plans")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlanDetailsResponse>> getAllPlans() {
        return ResponseEntity.ok(subscriptionService.getAllPlans());
    }

    @PostMapping("/upgrade")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<SubscriptionResponse> upgradeSubscription(@Valid @RequestBody UpgradeSubscriptionRequest request) {
        Long tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(subscriptionService.upgradeSubscription(
            tenantId,
            request.getPlan(),
            request.getBillingCycle().name()
        ));
    }
}
