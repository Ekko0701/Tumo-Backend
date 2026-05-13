package com.tumo.user.controller;

import com.tumo.user.dto.MyUserResponse;
import com.tumo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public MyUserResponse getMyUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        return userService.getMyUser(userId);
    }
}
