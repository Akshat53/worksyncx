package com.worksyncx.hrms.security;

import com.worksyncx.hrms.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Custom permission evaluator for fine-grained permission checks.
 * Allows use of hasPermission() in @PreAuthorize annotations.
 *
 * Usage examples:
 * - @PreAuthorize("hasPermission(null, 'EMPLOYEE:READ')")
 * - @PreAuthorize("hasPermission(#employeeId, 'Employee', 'READ_SELF')")
 */
@Component
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {

    /**
     * Check if the authenticated user has a specific permission.
     *
     * @param authentication The authentication object
     * @param targetDomainObject The target object (can be null for general permission checks)
     * @param permission The permission to check (e.g., "EMPLOYEE:READ" or "READ")
     * @return true if user has the permission
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Authentication is null or not authenticated");
            return false;
        }

        String permissionString = permission.toString();

        // Check if user is TENANT_ADMIN (has all permissions)
        if (hasAuthority(authentication, "ROLE_TENANT_ADMIN")) {
            log.debug("User is TENANT_ADMIN, granting permission: {}", permissionString);
            return true;
        }

        // Check if user has the specific permission
        boolean hasPermission = hasAuthority(authentication, permissionString);
        log.debug("Permission check for '{}': {}", permissionString, hasPermission);

        return hasPermission;
    }

    /**
     * Check if the authenticated user has permission on a specific domain object.
     *
     * @param authentication The authentication object
     * @param targetId The ID of the target object
     * @param targetType The type of the target object (e.g., "Employee", "Attendance")
     * @param permission The permission to check (e.g., "READ", "UPDATE", "READ_SELF")
     * @return true if user has the permission
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Authentication is null or not authenticated");
            return false;
        }

        String permissionString = permission.toString();
        String fullPermission = targetType.toUpperCase() + ":" + permissionString;

        // Check if user is TENANT_ADMIN (has all permissions)
        if (hasAuthority(authentication, "ROLE_TENANT_ADMIN")) {
            log.debug("User is TENANT_ADMIN, granting permission: {}", fullPermission);
            return true;
        }

        // Check if user has the specific permission
        boolean hasFullPermission = hasAuthority(authentication, fullPermission);

        // For _SELF permissions, also check if the target belongs to the user
        if (permissionString.endsWith("_SELF")) {
            // Get the user from authentication
            if (authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();

                // For employee self-access, check if targetId matches user's employee ID
                if (targetType.equalsIgnoreCase("Employee")) {
                    // Note: This is a simplified check. In a real implementation,
                    // you would fetch the employee and verify ownership
                    log.debug("Checking _SELF permission for Employee: {} against user ID: {}", targetId, user.getId());

                    // If user has the _SELF permission, they can only access their own data
                    // This would require additional logic to verify the targetId belongs to the user
                    if (hasFullPermission) {
                        // Here you would add logic to verify that targetId actually belongs to this user
                        // For now, we'll just check if they have the permission
                        return true;
                    }
                }
            }
        }

        log.debug("Permission check for '{}': {}", fullPermission, hasFullPermission);
        return hasFullPermission;
    }

    /**
     * Helper method to check if authentication has a specific authority
     */
    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(auth -> auth.equals(authority));
    }
}
