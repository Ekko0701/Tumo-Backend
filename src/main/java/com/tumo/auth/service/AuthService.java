package com.tumo.auth.service;

import com.tumo.auth.domain.RefreshToken;
import com.tumo.auth.dto.LoginRequest;
import com.tumo.auth.dto.LoginResponse;
import com.tumo.auth.dto.SignupRequest;
import com.tumo.auth.dto.SignupResponse;
import com.tumo.auth.dto.TokenRefreshRequest;
import com.tumo.auth.repository.RefreshTokenRepository;
import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.global.security.jwt.JwtProperties;
import com.tumo.global.security.jwt.JwtTokenProvider;
import com.tumo.user.domain.User;
import com.tumo.user.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

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

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        saveOrUpdateRefreshToken(user, refreshToken);

        return new LoginResponse(accessToken, refreshToken, TOKEN_TYPE);
    }

    private void saveOrUpdateRefreshToken(User user, String token) {
        LocalDateTime expiresAt = calculateRefreshTokenExpiresAt();

        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        refreshToken -> refreshToken.updateToken(token, expiresAt),
                        () -> refreshTokenRepository.save(new RefreshToken(user, token, expiresAt))
                );
    }

    @Transactional
    public LoginResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.refreshToken();

        if (!jwtTokenProvider.validateToken(requestRefreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserId(requestRefreshToken);
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (!refreshToken.getUser().getId().equals(userId) || refreshToken.isExpired(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);
        refreshToken.updateToken(newRefreshToken, calculateRefreshTokenExpiresAt());

        return new LoginResponse(newAccessToken, newRefreshToken, TOKEN_TYPE);
    }

    private LocalDateTime calculateRefreshTokenExpiresAt() {
        return LocalDateTime.now()
                .plus(Duration.ofMillis(jwtProperties.refreshTokenExpirationMillis()));
    }
}
