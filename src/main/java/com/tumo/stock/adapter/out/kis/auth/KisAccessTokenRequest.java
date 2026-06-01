package com.tumo.stock.adapter.out.kis.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tumo.stock.adapter.out.kis.config.KisProperties;

/**
 * KIS REST access token 발급 요청 값.
 *
 * @param grantType KIS access token 발급 방식
 * @param appKey KIS Developers에서 발급받은 app key
 * @param appSecret KIS Developers에서 발급받은 app secret
 */
record KisAccessTokenRequest(
        @JsonProperty("grant_type")
        String grantType,

        @JsonProperty("appkey")
        String appKey,

        @JsonProperty("appsecret")
        String appSecret
) {

    /**
     * KIS 설정 값으로 access token 발급 요청 값을 생성한다.
     *
     * @param properties KIS Open API 연동 설정 값
     * @return access token 발급 요청 값
     */
    static KisAccessTokenRequest from(KisProperties properties) {
        return new KisAccessTokenRequest(
                "client_credentials",
                properties.appKey(),
                properties.appSecret()
        );
    }
}
