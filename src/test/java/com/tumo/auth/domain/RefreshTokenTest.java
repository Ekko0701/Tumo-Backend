package com.tumo.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tumo.user.domain.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

    @Test
    void updateToken() {
        User user = new User("test@example.com", "encoded-password", "tester");
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(14);
        RefreshToken refreshToken = new RefreshToken(user, "old-refresh-token", expiresAt);
        LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(15);

        refreshToken.updateToken("new-refresh-token", newExpiresAt);

        assertThat(refreshToken.getToken()).isEqualTo("new-refresh-token");
        assertThat(refreshToken.getExpiresAt()).isEqualTo(newExpiresAt);
    }
}
