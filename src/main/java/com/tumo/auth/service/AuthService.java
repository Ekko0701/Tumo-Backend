package com.tumo.auth.service;

import com.tumo.auth.dto.LoginRequest;
import com.tumo.auth.dto.LoginResponse;
import com.tumo.auth.dto.SignupRequest;
import com.tumo.auth.dto.SignupResponse;
import com.tumo.auth.jwt.JwtTokenProvider;
import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.user.domain.User;
import com.tumo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
                request.email(),
                passwordHash,
                request.nickname()
        );

        User savedUser = userRepository.save(user);

        return SignupResponse.from(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());

        return new LoginResponse(accessToken, TOKEN_TYPE);
    }
}
