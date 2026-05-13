package com.tumo.auth.controller;

import com.tumo.auth.dto.LoginRequest;
import com.tumo.auth.dto.LoginResponse;
import com.tumo.auth.dto.SignupRequest;
import com.tumo.auth.dto.SignupResponse;
import com.tumo.auth.dto.TokenRefreshRequest;
import com.tumo.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임으로 신규 사용자를 생성합니다.")
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호를 검증하고 Access Token과 Refresh Token을 발급합니다.")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/token/refresh")
    @Operation(summary = "Access Token 재발급", description = "Refresh Token을 검증하고 새 Access Token과 Refresh Token을 발급합니다.")
    public LoginResponse refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자의 Refresh Token을 폐기합니다.")
    public void logout(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        authService.logout(userId);
    }
}
