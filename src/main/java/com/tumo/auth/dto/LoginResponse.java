package com.tumo.auth.dto;

/**
 * 로그인 응답 DTO.
 */
public record LoginResponse(
        /**
         * API 인증에 사용할 access token.
         */
        String accessToken,

        /**
         * 토큰 타입.
         */
        String tokenType
) {
}
