package com.tumo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Access Token 재발급 요청 DTO.
 */
@Schema(description = "Access Token 재발급 요청")
public record TokenRefreshRequest(
        /**
         * Access Token 재발급에 사용할 refresh token.
         */
        @Schema(description = "Access Token 재발급에 사용할 Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank(message = "Refresh Token은 필수입니다.")
        String refreshToken
) {
}
