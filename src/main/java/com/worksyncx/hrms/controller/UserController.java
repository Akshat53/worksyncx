package com.worksyncx.hrms.controller;

import com.worksyncx.hrms.dto.UserRequest;
import com.worksyncx.hrms.entity.User;
import com.worksyncx.hrms.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    public User createUser(@RequestBody UserRequest request) {
        return service.createUser(request);
    }

    @GetMapping
    public List<User> getAll() {
        return service.getAllUsers();
    }
}
