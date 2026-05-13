package com.tumo.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private static final String SECRET = "tumo-local-development-secret-key-must-be-at-least-32-bytes";

    @Test
    void createAccessToken() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(new JwtProperties(SECRET, 3600000L, 1209600000L));

        String accessToken = jwtTokenProvider.createAccessToken(1L);

        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtTokenProvider.getUserId(accessToken)).isEqualTo(1L);
    }

    @Test
    void createRefreshToken() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(new JwtProperties(SECRET, 3600000L, 1209600000L));

        String refreshToken = jwtTokenProvider.createRefreshToken(1L);

        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtTokenProvider.getUserId(refreshToken)).isEqualTo(1L);
    }

    @Test
    void validateTokenReturnsFalseWhenTokenIsInvalid() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(new JwtProperties(SECRET, 3600000L, 1209600000L));

        assertThat(jwtTokenProvider.validateToken("invalid-token")).isFalse();
    }

    @Test
    void validateTokenReturnsFalseWhenTokenIsExpired() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(new JwtProperties(SECRET, -1000L, -1000L));

        String expiredToken = jwtTokenProvider.createAccessToken(1L);

        assertThat(jwtTokenProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    void validateTokenReturnsFalseWhenRefreshTokenIsExpired() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(new JwtProperties(SECRET, 3600000L, -1000L));

        String expiredToken = jwtTokenProvider.createRefreshToken(1L);

        assertThat(jwtTokenProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    void getUserIdThrowsExceptionWhenTokenIsInvalid() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(new JwtProperties(SECRET, 3600000L, 1209600000L));

        assertThatThrownBy(() -> jwtTokenProvider.getUserId("invalid-token"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN)
                );
    }
}
