package com.worksyncx.hrms.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private Long tenantId;
    private String email;
    private String firstName;
    private String lastName;
    private List<AuthRoleDto> roles;
    private Boolean mustChangePassword;
    private Set<String> subscriptionModules;

    public AuthResponse(String token, Long userId, Long tenantId, String email,
                       String firstName, String lastName, List<AuthRoleDto> roles,
                       Boolean mustChangePassword, Set<String> subscriptionModules) {
        this.token = token;
        this.userId = userId;
        this.tenantId = tenantId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
        this.mustChangePassword = mustChangePassword;
        this.subscriptionModules = subscriptionModules;
    }
}
