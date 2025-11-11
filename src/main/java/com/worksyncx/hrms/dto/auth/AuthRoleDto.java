package com.worksyncx.hrms.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRoleDto {
    private Long id;
    private String name;
    private String description;
    private List<AuthPermissionDto> permissions;
}
