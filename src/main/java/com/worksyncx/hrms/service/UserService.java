package com.worksyncx.hrms.service;


import com.worksyncx.hrms.dto.UserRequest;
import com.worksyncx.hrms.dto.common.PageResponse;
import com.worksyncx.hrms.dto.user.AssignRolesRequest;
import com.worksyncx.hrms.dto.user.UserResponse;
import com.worksyncx.hrms.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface UserService {
    User createUser(UserRequest request);
    List<User> getAllUsers();
    List<UserResponse> getAllUsersWithRoles();
    UserResponse getUserById(Long id);
    UserResponse assignRoles(Long userId, AssignRolesRequest request);
    UserResponse addRolesToUser(Long userId, Set<Long> roleIds);
    UserResponse removeRoleFromUser(Long userId, Long roleId);

    // Paginated method
    PageResponse<UserResponse> getAllUsersWithRolesPaginated(Pageable pageable);
}
