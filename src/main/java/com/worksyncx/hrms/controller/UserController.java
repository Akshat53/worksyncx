package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.UserRequest;
import com.worksyncx.hrms.dto.user.AssignRolesRequest;
import com.worksyncx.hrms.dto.user.UserResponse;
import com.worksyncx.hrms.entity.User;
import com.worksyncx.hrms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public User createUser(@RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    @GetMapping
    public List<User> getAll() {
        return userService.getAllUsers();
    }

    // ==================== User-Role Management Endpoints ====================

    @GetMapping("/with-roles")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsersWithRoles() {
        List<UserResponse> users = userService.getAllUsersWithRoles();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User not found", "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<?> assignRoles(
        @PathVariable Long id,
        @Valid @RequestBody AssignRolesRequest request
    ) {
        try {
            UserResponse user = userService.assignRoles(id, request);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to assign roles", "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/roles/add")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<?> addRoles(
        @PathVariable Long id,
        @Valid @RequestBody AssignRolesRequest request
    ) {
        try {
            UserResponse user = userService.addRolesToUser(id, request.getRoleIds());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to add roles", "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    @PreAuthorize("hasAuthority('ROLE_TENANT_ADMIN')")
    public ResponseEntity<?> removeRole(
        @PathVariable Long id,
        @PathVariable Long roleId
    ) {
        try {
            UserResponse user = userService.removeRoleFromUser(id, roleId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to remove role", "message", e.getMessage()));
        }
    }
}
