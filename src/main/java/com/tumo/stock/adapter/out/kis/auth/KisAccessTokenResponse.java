package com.tumo.stock.adapter.out.kis.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * KIS REST access token 발급 응답 값.
 *
 * @param accessToken KIS REST API 호출에 사용할 access token
 * @param accessTokenTokenExpired access token 만료 시각
 * @param expiresIn access token 만료까지 남은 시간
 */
record KisAccessTokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("access_token_token_expired")
        String accessTokenTokenExpired,

        @JsonProperty("expires_in")
        Long expiresIn
) {
}
