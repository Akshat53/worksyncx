package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.permission.PermissionRequest;
import com.worksyncx.hrms.dto.permission.PermissionResponse;
import com.worksyncx.hrms.service.permission.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('PERMISSION:CREATE')")
    public ResponseEntity<?> createPermission(@Valid @RequestBody PermissionRequest request) {
        try {
            PermissionResponse response = permissionService.createPermission(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to create permission", "message", e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('PERMISSION:READ')")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/accessible")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('PERMISSION:READ')")
    public ResponseEntity<List<PermissionResponse>> getAccessiblePermissions() {
        List<PermissionResponse> permissions = permissionService.getAccessiblePermissions();
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/module/{module}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('PERMISSION:READ')")
    public ResponseEntity<List<PermissionResponse>> getPermissionsByModule(@PathVariable String module) {
        List<PermissionResponse> permissions = permissionService.getPermissionsByModule(module);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('PERMISSION:READ')")
    public ResponseEntity<?> getPermissionById(@PathVariable Long id) {
        try {
            PermissionResponse response = permissionService.getPermissionById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Permission not found", "message", e.getMessage()));
        }
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('PERMISSION:READ')")
    public ResponseEntity<?> getPermissionByCode(@PathVariable String code) {
        try {
            PermissionResponse response = permissionService.getPermissionByCode(code);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Permission not found", "message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('PERMISSION:UPDATE')")
    public ResponseEntity<?> updatePermission(
        @PathVariable Long id,
        @Valid @RequestBody PermissionRequest request
    ) {
        try {
            PermissionResponse response = permissionService.updatePermission(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to update permission", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN') or hasAuthority('PERMISSION:DELETE')")
    public ResponseEntity<?> deletePermission(@PathVariable Long id) {
        try {
            permissionService.deletePermission(id);
            return ResponseEntity.ok(Map.of("message", "Permission deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to delete permission", "message", e.getMessage()));
        }
    }
}
