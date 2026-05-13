package com.tumo.user.controller;

import com.tumo.user.dto.MyUserResponse;
import com.tumo.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "User", description = "사용자 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "Access Token의 사용자 식별자로 내 정보를 조회합니다.")
    public MyUserResponse getMyUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        return userService.getMyUser(userId);
    }
}
