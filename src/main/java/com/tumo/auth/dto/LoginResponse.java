package com.tumo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 로그인 응답 DTO.
 */
@Schema(description = "로그인 응답")
public record LoginResponse(
        /**
         * API 인증에 사용할 access token.
         */
        @Schema(description = "API 인증에 사용할 Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        /**
         * Access token 재발급에 사용할 refresh token.
         */
        @Schema(description = "Access Token 재발급에 사용할 Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,

        /**
         * 토큰 타입.
         */
        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType
) {
}
