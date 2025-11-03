package com.worksyncx.hrms.service;


import com.worksyncx.hrms.dto.UserRequest;
import com.worksyncx.hrms.entity.User;

import java.util.List;

public interface UserService {
    User createUser(UserRequest request);
    List<User> getAllUsers();
}
