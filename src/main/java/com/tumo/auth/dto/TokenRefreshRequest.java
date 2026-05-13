package com.tumo.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Access Token 재발급 요청 DTO.
 */
public record TokenRefreshRequest(
        /**
         * Access Token 재발급에 사용할 refresh token.
         */
        @NotBlank(message = "Refresh Token은 필수입니다.")
        String refreshToken
) {
}
