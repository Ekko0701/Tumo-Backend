package com.tumo.stock.adapter.out.kis.websocket.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * KIS WebSocket approval key 발급 응답 값.
 *
 * @param approvalKey KIS WebSocket 접속과 구독 요청에 사용할 approval key
 */
record KisApprovalKeyResponse(
        @JsonProperty("approval_key")
        String approvalKey
) {
}
