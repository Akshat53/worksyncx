package com.worksyncx.hrms.service.impl;

import com.worksyncx.hrms.dto.UserRequest;
import com.worksyncx.hrms.entity.User;
import com.worksyncx.hrms.repository.UserRepository;
import com.worksyncx.hrms.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repo;

    public UserServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public User createUser(UserRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFullName());
        user.setPassword(request.getPassword());
        return repo.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return repo.findAll();
    }
}
