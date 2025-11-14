package com.worksyncx.hrms.service.permission;

import com.worksyncx.hrms.dto.permission.PermissionRequest;
import com.worksyncx.hrms.dto.permission.PermissionResponse;
import com.worksyncx.hrms.entity.Permission;
import com.worksyncx.hrms.entity.Subscription;
import com.worksyncx.hrms.repository.PermissionRepository;
import com.worksyncx.hrms.repository.SubscriptionRepository;
import com.worksyncx.hrms.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        // Check if permission code already exists
        if (permissionRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Permission with code '" + request.getCode() + "' already exists");
        }

        Permission permission = new Permission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setModule(request.getModule());
        permission.setAction(request.getAction());
        permission.setDescription(request.getDescription());

        permission = permissionRepository.save(permission);
        log.info("Created new permission: {}", permission.getCode());

        return mapToResponse(permission);
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get permissions that are accessible based on tenant's subscription modules.
     * Only returns permissions for modules that the tenant has subscribed to,
     * plus system-level permissions (USER, ROLE, PERMISSION).
     */
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAccessiblePermissions() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            log.warn("No tenant context found, returning all permissions");
            return getAllPermissions();
        }

        // Get tenant's subscription
        Subscription subscription = subscriptionRepository.findByTenantId(tenantId)
            .orElse(null);

        if (subscription == null || subscription.getModules() == null) {
            log.warn("No subscription found for tenant {}, returning system permissions only", tenantId);
            // Return only system-level permissions (USER, ROLE, PERMISSION)
            return permissionRepository.findAll()
                .stream()
                .filter(p -> "USER".equals(p.getModule()) ||
                            "ROLE".equals(p.getModule()) ||
                            "PERMISSION".equals(p.getModule()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        }

        // Get allowed modules from subscription and map them to permission module names
        Set<String> allowedModules = new HashSet<>();
        for (String module : subscription.getModules()) {
            allowedModules.add(mapSubscriptionModuleToPermissionModule(module));
        }

        // Always include system-level permissions
        allowedModules.add("USER");
        allowedModules.add("ROLE");
        allowedModules.add("PERMISSION");

        log.info("Filtering permissions for tenant {} with modules: {}", tenantId, allowedModules);

        // Filter permissions by allowed modules
        return permissionRepository.findAll()
            .stream()
            .filter(p -> allowedModules.contains(p.getModule()))
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissionsByModule(String module) {
        return permissionRepository.findByModule(module)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found with ID: " + id));
        return mapToResponse(permission);
    }

    @Transactional(readOnly = true)
    public PermissionResponse getPermissionByCode(String code) {
        Permission permission = permissionRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Permission not found with code: " + code));
        return mapToResponse(permission);
    }

    @Transactional
    public PermissionResponse updatePermission(Long id, PermissionRequest request) {
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found with ID: " + id));

        // Check if new code conflicts with existing permission
        if (!permission.getCode().equals(request.getCode()) &&
            permissionRepository.existsByCode(request.getCode())) {
            throw new RuntimeException("Permission with code '" + request.getCode() + "' already exists");
        }

        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setModule(request.getModule());
        permission.setAction(request.getAction());
        permission.setDescription(request.getDescription());

        permission = permissionRepository.save(permission);
        log.info("Updated permission: {}", permission.getCode());

        return mapToResponse(permission);
    }

    @Transactional
    public void deletePermission(Long id) {
        Permission permission = permissionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Permission not found with ID: " + id));

        // Check if permission is assigned to any roles
        long roleCount = permission.getId() != null ?
            permissionRepository.countRolesUsingPermission(permission.getId()) : 0;

        if (roleCount > 0) {
            throw new RuntimeException(
                "Cannot delete permission '" + permission.getCode() +
                "' as it is currently assigned to " + roleCount + " role(s). " +
                "Please remove it from all roles before deleting."
            );
        }

        permissionRepository.delete(permission);
        log.info("Deleted permission: {}", permission.getCode());
    }

    /**
     * Maps subscription module names to permission module names.
     * Subscription modules use plural/descriptive names (e.g., DEPARTMENTS, LEAVE_MANAGEMENT)
     * Permission modules use singular names (e.g., DEPARTMENT, LEAVE)
     * Case-insensitive to handle legacy lowercase module names.
     */
    private String mapSubscriptionModuleToPermissionModule(String subscriptionModule) {
        // Convert to uppercase for consistent matching
        String upperModule = subscriptionModule.toUpperCase();

        return switch (upperModule) {
            case "DEPARTMENTS" -> "DEPARTMENT";
            case "DESIGNATIONS" -> "DESIGNATION";
            case "EMPLOYEES", "EMPLOYEE" -> "EMPLOYEE";  // Support both forms
            case "LEAVE_MANAGEMENT", "LEAVE" -> "LEAVE";  // Support both forms
            case "SHIFTS", "SHIFT" -> "SHIFT";  // Support both forms
            case "ATTENDANCE" -> "ATTENDANCE";
            case "PAYROLL" -> "PAYROLL";
            case "REPORTS" -> "REPORTS";
            default -> upperModule; // Return uppercase version if no mapping exists
        };
    }

    private PermissionResponse mapToResponse(Permission permission) {
        return PermissionResponse.builder()
            .id(permission.getId())
            .code(permission.getCode())
            .name(permission.getName())
            .module(permission.getModule())
            .action(permission.getAction())
            .description(permission.getDescription())
            .createdAt(permission.getCreatedAt())
            .build();
    }
}
