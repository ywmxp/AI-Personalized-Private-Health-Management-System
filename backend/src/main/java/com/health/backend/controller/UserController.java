package com.health.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.health.backend.dto.ApiResponse;
import com.health.backend.dto.CurrentUserResponse;
import com.health.backend.security.JwtUser;
import com.health.backend.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(@AuthenticationPrincipal JwtUser currentUser) {
        return ApiResponse.success(userService.getCurrentUser(currentUser.userId()));
    }
}
