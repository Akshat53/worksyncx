package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.auth.AuthPermissionDto;
import com.worksyncx.hrms.dto.auth.AuthResponse;
import com.worksyncx.hrms.dto.auth.AuthRoleDto;
import com.worksyncx.hrms.dto.auth.ChangePasswordRequest;
import com.worksyncx.hrms.dto.auth.LoginRequest;
import com.worksyncx.hrms.dto.auth.RegisterRequest;
import com.worksyncx.hrms.entity.User;
import com.worksyncx.hrms.service.auth.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            AuthResponse response = authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        User user = (User) authentication.getPrincipal();

        // Convert roles to AuthRoleDto with permissions (same format as login response)
        var roleDtos = user.getRoles().stream()
            .map(role -> {
                var permissionDtos = role.getPermissions().stream()
                    .map(permission -> {
                        var dto = new AuthPermissionDto();
                        dto.setId(permission.getId());
                        dto.setCode(permission.getCode());
                        dto.setName(permission.getName());
                        dto.setModule(permission.getModule());
                        dto.setAction(permission.getAction());
                        return dto;
                    })
                    .collect(Collectors.toList());

                var roleDto = new AuthRoleDto();
                roleDto.setId(role.getId());
                roleDto.setName(role.getName());
                roleDto.setDescription(role.getDescription());
                roleDto.setPermissions(permissionDtos);
                return roleDto;
            })
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("tenantId", user.getTenantId());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("isActive", user.getIsActive());
        response.put("roles", roleDtos);  // Now returns full role objects with permissions

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            User user = (User) authentication.getPrincipal();
            authService.changePassword(user.getId(), request);

            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Password change failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}
