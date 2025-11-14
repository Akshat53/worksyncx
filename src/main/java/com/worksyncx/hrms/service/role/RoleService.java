package com.worksyncx.hrms.service.role;

import com.worksyncx.hrms.security.TenantContext;
import com.worksyncx.hrms.dto.common.PageResponse;
import com.worksyncx.hrms.dto.permission.PermissionResponse;
import com.worksyncx.hrms.dto.role.AssignPermissionsRequest;
import com.worksyncx.hrms.dto.role.RoleRequest;
import com.worksyncx.hrms.dto.role.RoleResponse;
import com.worksyncx.hrms.entity.Permission;
import com.worksyncx.hrms.entity.Role;
import com.worksyncx.hrms.repository.PermissionRepository;
import com.worksyncx.hrms.repository.RoleRepository;
import com.worksyncx.hrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        Long tenantId = TenantContext.getTenantId();

        // Check if role with same name already exists for this tenant
        if (roleRepository.existsByTenantIdAndName(tenantId, request.getName())) {
            throw new RuntimeException("Role with name '" + request.getName() + "' already exists for this tenant");
        }

        Role role = new Role();
        role.setTenantId(tenantId);
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setIsSystemRole(request.getIsSystemRole() != null ? request.getIsSystemRole() : false);
        role.setCreatedBy(TenantContext.getUserId());
        role.setPermissions(new HashSet<>());

        role = roleRepository.save(role);
        log.info("Created new role: {} for tenant: {}", role.getName(), tenantId);

        return mapToResponse(role);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        Long tenantId = TenantContext.getTenantId();
        return roleRepository.findByTenantId(tenantId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        Role role = roleRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));
        return mapToResponse(role);
    }

    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Long tenantId = TenantContext.getTenantId();

        Role role = roleRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));

        // Prevent updating system roles
        if (role.getIsSystemRole()) {
            throw new RuntimeException("System roles cannot be modified");
        }

        // Check if new name conflicts with existing role
        if (!role.getName().equals(request.getName()) &&
            roleRepository.existsByTenantIdAndName(tenantId, request.getName())) {
            throw new RuntimeException("Role with name '" + request.getName() + "' already exists for this tenant");
        }

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setUpdatedBy(TenantContext.getUserId());

        role = roleRepository.save(role);
        log.info("Updated role: {} for tenant: {}", role.getName(), tenantId);

        return mapToResponse(role);
    }

    @Transactional
    public void deleteRole(Long id) {
        Long tenantId = TenantContext.getTenantId();

        Role role = roleRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new RuntimeException("Role not found with ID: " + id));

        // Prevent deleting system roles
        if (role.getIsSystemRole()) {
            throw new RuntimeException("System roles cannot be deleted");
        }

        // Check if role is assigned to any users
        long userCount = role.getId() != null ?
            userRepository.countUsersWithRole(role.getId()) : 0;

        if (userCount > 0) {
            throw new RuntimeException(
                "Cannot delete role '" + role.getName() +
                "' as it is currently assigned to " + userCount + " user(s). " +
                "Please remove it from all users before deleting."
            );
        }

        roleRepository.delete(role);
        log.info("Deleted role: {} for tenant: {}", role.getName(), tenantId);
    }

    @Transactional
    public RoleResponse assignPermissions(Long roleId, AssignPermissionsRequest request) {
        Long tenantId = TenantContext.getTenantId();

        Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
            .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        // Get all permissions by IDs
        List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());

        if (permissions.size() != request.getPermissionIds().size()) {
            throw new RuntimeException("One or more permission IDs are invalid");
        }

        // Replace all permissions
        role.setPermissions(new HashSet<>(permissions));
        role.setUpdatedBy(TenantContext.getUserId());

        role = roleRepository.save(role);
        log.info("Assigned {} permissions to role: {} for tenant: {}",
            permissions.size(), role.getName(), tenantId);

        return mapToResponse(role);
    }

    @Transactional
    public RoleResponse addPermissionsToRole(Long roleId, Set<Long> permissionIds) {
        Long tenantId = TenantContext.getTenantId();

        Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
            .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        // Get permissions to add
        List<Permission> permissionsToAdd = permissionRepository.findAllById(permissionIds);

        if (permissionsToAdd.size() != permissionIds.size()) {
            throw new RuntimeException("One or more permission IDs are invalid");
        }

        // Add new permissions
        role.getPermissions().addAll(permissionsToAdd);
        role.setUpdatedBy(TenantContext.getUserId());

        role = roleRepository.save(role);
        log.info("Added {} permissions to role: {} for tenant: {}",
            permissionsToAdd.size(), role.getName(), tenantId);

        return mapToResponse(role);
    }

    @Transactional
    public RoleResponse removePermissionFromRole(Long roleId, Long permissionId) {
        Long tenantId = TenantContext.getTenantId();

        Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
            .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("Permission not found with ID: " + permissionId));

        role.getPermissions().remove(permission);
        role.setUpdatedBy(TenantContext.getUserId());

        role = roleRepository.save(role);
        log.info("Removed permission {} from role: {} for tenant: {}",
            permission.getCode(), role.getName(), tenantId);

        return mapToResponse(role);
    }

    @Transactional(readOnly = true)
    public Set<PermissionResponse> getRolePermissions(Long roleId) {
        Long tenantId = TenantContext.getTenantId();

        Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
            .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        return role.getPermissions().stream()
            .map(this::mapPermissionToResponse)
            .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> getAllRolesPaginated(Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Page<Role> page = roleRepository.findByTenantId(tenantId, pageable);
        return mapToPageResponse(page);
    }

    // ==================== Mappers ====================

    private PageResponse<RoleResponse> mapToPageResponse(Page<Role> page) {
        List<RoleResponse> content = page.getContent()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return PageResponse.<RoleResponse>builder()
            .content(content)
            .pageNumber(page.getNumber())
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .numberOfElements(page.getNumberOfElements())
            .empty(page.isEmpty())
            .build();
    }

    private RoleResponse mapToResponse(Role role) {
        Set<PermissionResponse> permissionResponses = role.getPermissions() != null
            ? role.getPermissions().stream()
                .map(this::mapPermissionToResponse)
                .collect(Collectors.toSet())
            : new HashSet<>();

        return RoleResponse.builder()
            .id(role.getId())
            .name(role.getName())
            .description(role.getDescription())
            .isSystemRole(role.getIsSystemRole())
            .tenantId(role.getTenantId())
            .permissions(permissionResponses)
            .createdAt(role.getCreatedAt())
            .updatedAt(role.getUpdatedAt())
            .build();
    }

    private PermissionResponse mapPermissionToResponse(Permission permission) {
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
