package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.common.PageResponse;
import com.worksyncx.hrms.dto.permission.PermissionResponse;
import com.worksyncx.hrms.dto.role.AssignPermissionsRequest;
import com.worksyncx.hrms.dto.role.RoleRequest;
import com.worksyncx.hrms.dto.role.RoleResponse;
import com.worksyncx.hrms.service.role.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('ROLE:CREATE')")
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleRequest request) {
        try {
            RoleResponse response = roleService.createRole(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to create role", "message", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('ROLE:READ')")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('ROLE:READ')")
    public ResponseEntity<?> getRoleById(@PathVariable Long id) {
        try {
            RoleResponse response = roleService.getRoleById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Role not found", "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('ROLE:UPDATE')")
    public ResponseEntity<?> updateRole(
        @PathVariable Long id,
        @Valid @RequestBody RoleRequest request
    ) {
        try {
            RoleResponse response = roleService.updateRole(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to update role", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('ROLE:DELETE')")
    public ResponseEntity<?> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.ok(Map.of("message", "Role deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to delete role", "message", e.getMessage()));
        }
    }

    // ==================== Permission Management Endpoints ====================

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('ROLE:ASSIGN_PERMISSION')")
    public ResponseEntity<?> assignPermissions(
        @PathVariable Long id,
        @Valid @RequestBody AssignPermissionsRequest request
    ) {
        try {
            RoleResponse response = roleService.assignPermissions(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to assign permissions", "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/permissions/add")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('ROLE:ASSIGN_PERMISSION')")
    public ResponseEntity<?> addPermissions(
        @PathVariable Long id,
        @Valid @RequestBody AssignPermissionsRequest request
    ) {
        try {
            RoleResponse response = roleService.addPermissionsToRole(id, request.getPermissionIds());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to add permissions", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('ROLE:ASSIGN_PERMISSION')")
    public ResponseEntity<?> removePermission(
        @PathVariable Long id,
        @PathVariable Long permissionId
    ) {
        try {
            RoleResponse response = roleService.removePermissionFromRole(id, permissionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to remove permission", "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('ROLE:READ')")
    public ResponseEntity<?> getRolePermissions(@PathVariable Long id) {
        try {
            Set<PermissionResponse> permissions = roleService.getRolePermissions(id);
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Role not found", "message", e.getMessage()));
        }
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('ROLE:READ')")
    public ResponseEntity<?> getAllRolesPaginated(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        try {
            // Create sort object
            Sort sort = sortDirection.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

            // Create pageable object
            Pageable pageable = PageRequest.of(page, size, sort);

            PageResponse<RoleResponse> rolesPage = roleService.getAllRolesPaginated(pageable);

            return ResponseEntity.ok(rolesPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to get roles", "message", e.getMessage()));
        }
    }
}
