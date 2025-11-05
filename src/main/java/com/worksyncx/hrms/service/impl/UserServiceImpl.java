package com.worksyncx.hrms.service.impl;

import com.worksyncx.hrms.dto.UserRequest;
import com.worksyncx.hrms.dto.permission.PermissionResponse;
import com.worksyncx.hrms.dto.role.RoleResponse;
import com.worksyncx.hrms.dto.user.AssignRolesRequest;
import com.worksyncx.hrms.dto.user.UserResponse;
import com.worksyncx.hrms.entity.Permission;
import com.worksyncx.hrms.entity.Role;
import com.worksyncx.hrms.entity.User;
import com.worksyncx.hrms.repository.RoleRepository;
import com.worksyncx.hrms.repository.UserRepository;
import com.worksyncx.hrms.security.TenantContext;
import com.worksyncx.hrms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public User createUser(UserRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFullName());
        user.setPassword(request.getPassword());
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsersWithRoles() {
        Long tenantId = TenantContext.getTenantId();
        List<User> users = userRepository.findByTenantId(tenantId);
        return users.stream()
            .map(this::mapToUserResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        Long tenantId = TenantContext.getTenantId();
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Verify user belongs to current tenant
        if (!user.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Access denied: User does not belong to your tenant");
        }

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse assignRoles(Long userId, AssignRolesRequest request) {
        Long tenantId = TenantContext.getTenantId();

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Verify user belongs to current tenant
        if (!user.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Access denied: User does not belong to your tenant");
        }

        // Fetch all roles and verify they belong to the current tenant
        Set<Role> roles = new HashSet<>();
        for (Long roleId : request.getRoleIds()) {
            Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId + " in your tenant"));
            roles.add(role);
        }

        // Replace all roles
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse addRolesToUser(Long userId, Set<Long> roleIds) {
        Long tenantId = TenantContext.getTenantId();

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Verify user belongs to current tenant
        if (!user.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Access denied: User does not belong to your tenant");
        }

        // Add roles to existing ones (verify they belong to the current tenant)
        for (Long roleId : roleIds) {
            Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId + " in your tenant"));
            user.getRoles().add(role);
        }

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse removeRoleFromUser(Long userId, Long roleId) {
        Long tenantId = TenantContext.getTenantId();

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Verify user belongs to current tenant
        if (!user.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Access denied: User does not belong to your tenant");
        }

        Role role = roleRepository.findByIdAndTenantId(roleId, tenantId)
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId + " in your tenant"));

        user.getRoles().remove(role);
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    // Helper method to map User entity to UserResponse DTO
    private UserResponse mapToUserResponse(User user) {
        Set<RoleResponse> roleResponses = user.getRoles().stream()
            .map(this::mapToRoleResponse)
            .collect(Collectors.toSet());

        return UserResponse.builder()
            .id(user.getId())
            .tenantId(user.getTenantId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .fullName(user.getFullName())
            .isActive(user.getIsActive())
            .mustChangePassword(user.getMustChangePassword())
            .lastLogin(user.getLastLogin())
            .roles(roleResponses)
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    // Helper method to map Role entity to RoleResponse DTO
    private RoleResponse mapToRoleResponse(Role role) {
        Set<PermissionResponse> permissionResponses = role.getPermissions().stream()
            .map(this::mapToPermissionResponse)
            .collect(Collectors.toSet());

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

    // Helper method to map Permission entity to PermissionResponse DTO
    private PermissionResponse mapToPermissionResponse(Permission permission) {
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
