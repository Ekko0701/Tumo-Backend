package com.tumo.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tumo.auth.domain.RefreshToken;
import com.tumo.auth.dto.LoginRequest;
import com.tumo.auth.dto.LoginResponse;
import com.tumo.auth.dto.TokenRefreshRequest;
import com.tumo.auth.repository.RefreshTokenRepository;
import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.global.security.jwt.JwtProperties;
import com.tumo.global.security.jwt.JwtTokenProvider;
import com.tumo.user.domain.User;
import com.tumo.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginSavesRefreshTokenWhenRefreshTokenDoesNotExist() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "encoded-password")).willReturn(true);
        given(jwtTokenProvider.createAccessToken(1L)).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(1L)).willReturn("refresh-token");
        given(jwtProperties.refreshTokenExpirationMillis()).willReturn(1209600000L);
        given(refreshTokenRepository.findByUser(user)).willReturn(Optional.empty());

        LoginResponse response = authService.login(new LoginRequest("test@example.com", "password123"));

        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
        RefreshToken savedRefreshToken = refreshTokenCaptor.getValue();
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(savedRefreshToken.getUser()).isEqualTo(user);
        assertThat(savedRefreshToken.getToken()).isEqualTo("refresh-token");
        assertThat(savedRefreshToken.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void loginUpdatesRefreshTokenWhenRefreshTokenExists() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        RefreshToken refreshToken = new RefreshToken(
                user,
                "old-refresh-token",
                LocalDateTime.now().plusDays(1)
        );
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "encoded-password")).willReturn(true);
        given(jwtTokenProvider.createAccessToken(1L)).willReturn("access-token");
        given(jwtTokenProvider.createRefreshToken(1L)).willReturn("new-refresh-token");
        given(jwtProperties.refreshTokenExpirationMillis()).willReturn(1209600000L);
        given(refreshTokenRepository.findByUser(user)).willReturn(Optional.of(refreshToken));

        LoginResponse response = authService.login(new LoginRequest("test@example.com", "password123"));

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(refreshToken.getToken()).isEqualTo("new-refresh-token");
        assertThat(refreshToken.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void refreshToken() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        RefreshToken refreshToken = new RefreshToken(
                user,
                "old-refresh-token",
                LocalDateTime.now().plusDays(1)
        );
        given(jwtTokenProvider.validateToken("old-refresh-token")).willReturn(true);
        given(jwtTokenProvider.getUserId("old-refresh-token")).willReturn(1L);
        given(refreshTokenRepository.findByToken("old-refresh-token")).willReturn(Optional.of(refreshToken));
        given(jwtTokenProvider.createAccessToken(1L)).willReturn("new-access-token");
        given(jwtTokenProvider.createRefreshToken(1L)).willReturn("new-refresh-token");
        given(jwtProperties.refreshTokenExpirationMillis()).willReturn(1209600000L);

        LoginResponse response = authService.refreshToken(new TokenRefreshRequest("old-refresh-token"));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(refreshToken.getToken()).isEqualTo("new-refresh-token");
        assertThat(refreshToken.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    void refreshTokenThrowsExceptionWhenJwtIsInvalid() {
        given(jwtTokenProvider.validateToken("invalid-refresh-token")).willReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(new TokenRefreshRequest("invalid-refresh-token")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN)
                );

        verify(refreshTokenRepository, never()).findByToken("invalid-refresh-token");
    }

    @Test
    void refreshTokenThrowsExceptionWhenStoredTokenDoesNotExist() {
        given(jwtTokenProvider.validateToken("missing-refresh-token")).willReturn(true);
        given(jwtTokenProvider.getUserId("missing-refresh-token")).willReturn(1L);
        given(refreshTokenRepository.findByToken("missing-refresh-token")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(new TokenRefreshRequest("missing-refresh-token")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN)
                );
    }

    @Test
    void refreshTokenThrowsExceptionWhenStoredTokenIsExpired() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        RefreshToken refreshToken = new RefreshToken(
                user,
                "expired-refresh-token",
                LocalDateTime.now().minusSeconds(1)
        );
        given(jwtTokenProvider.validateToken("expired-refresh-token")).willReturn(true);
        given(jwtTokenProvider.getUserId("expired-refresh-token")).willReturn(1L);
        given(refreshTokenRepository.findByToken("expired-refresh-token")).willReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.refreshToken(new TokenRefreshRequest("expired-refresh-token")))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN)
                );
    }

    @Test
    void logoutDeletesRefreshToken() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        authService.logout(1L);

        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void logoutThrowsExceptionWhenUserDoesNotExist() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout(1L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND)
                );

        verify(refreshTokenRepository, never()).deleteByUser(any(User.class));
    }
}
