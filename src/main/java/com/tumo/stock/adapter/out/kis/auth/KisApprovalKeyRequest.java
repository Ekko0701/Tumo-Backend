package com.tumo.stock.adapter.out.kis.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tumo.stock.adapter.out.kis.config.KisProperties;

/**
 * KIS WebSocket approval key 발급 요청 값.
 *
 * @param grantType KIS approval key 발급 방식
 * @param appKey KIS Developers에서 발급받은 app key
 * @param secretKey KIS Developers에서 발급받은 app secret
 */
record KisApprovalKeyRequest(
        @JsonProperty("grant_type")
        String grantType,

        @JsonProperty("appkey")
        String appKey,

        @JsonProperty("secretkey")
        String secretKey
) {

    /**
     * KIS 설정 값으로 approval key 발급 요청 값을 생성한다.
     *
     * @param properties KIS Open API 연동 설정 값
     * @return approval key 발급 요청 값
     */
    static KisApprovalKeyRequest from(KisProperties properties) {
        return new KisApprovalKeyRequest(
                "client_credentials",
                properties.appKey(),
                properties.appSecret()
        );
    }
}
