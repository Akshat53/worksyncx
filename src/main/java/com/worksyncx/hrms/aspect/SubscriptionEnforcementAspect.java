package com.worksyncx.hrms.aspect;

import com.worksyncx.hrms.annotation.RequiresModule;
import com.worksyncx.hrms.enums.Module;
import com.worksyncx.hrms.exception.SubscriptionException;
import com.worksyncx.hrms.security.TenantContext;
import com.worksyncx.hrms.service.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect that intercepts methods annotated with @RequiresModule
 * and validates that the tenant has access to the required module.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionEnforcementAspect {

    private final SubscriptionService subscriptionService;

    /**
     * Intercepts method calls annotated with @RequiresModule and checks if the tenant
     * has access to the specified module before allowing method execution.
     *
     * @param joinPoint the join point representing the method execution
     * @throws SubscriptionException if tenant doesn't have access to the required module
     */
    @Before("@annotation(com.worksyncx.hrms.annotation.RequiresModule)")
    public void checkModuleAccess(JoinPoint joinPoint) {
        Long tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            log.error("TenantContext is null. This should not happen for authenticated requests.");
            throw new SubscriptionException("Tenant context not found. Please authenticate.");
        }

        // Get the method signature to extract the annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Get the RequiresModule annotation
        RequiresModule requiresModule = method.getAnnotation(RequiresModule.class);
        if (requiresModule == null) {
            // This shouldn't happen since the pointcut matched, but handle it gracefully
            log.warn("@RequiresModule annotation not found on method: {}", method.getName());
            return;
        }

        Module requiredModule = requiresModule.value();
        String moduleName = requiredModule.name();

        log.debug("Checking module access for tenant: {}, module: {}", tenantId, moduleName);

        // Check if tenant has access to the module
        boolean hasAccess = subscriptionService.hasModuleAccess(tenantId, moduleName);

        if (!hasAccess) {
            log.warn("Access denied for tenant: {} to module: {}. Subscription does not include this module.",
                     tenantId, moduleName);
            throw new SubscriptionException(
                String.format("Access denied. The '%s' module is not available in your subscription plan. Please upgrade to access this feature.",
                             moduleName)
            );
        }

        log.debug("Module access granted for tenant: {}, module: {}", tenantId, moduleName);
    }
}
