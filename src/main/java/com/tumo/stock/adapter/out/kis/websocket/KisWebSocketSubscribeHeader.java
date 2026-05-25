package com.tumo.stock.adapter.out.kis.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * KIS WebSocket 구독 요청 header.
 *
 * @param approvalKey KIS WebSocket 접속과 구독 요청에 사용할 approval key
 * @param customerType KIS 고객 타입
 * @param transactionType KIS WebSocket 거래 타입
 * @param contentType KIS WebSocket 메시지 인코딩 타입
 */
record KisWebSocketSubscribeHeader(
        @JsonProperty("approval_key")
        String approvalKey,

        @JsonProperty("custtype")
        String customerType,

        @JsonProperty("tr_type")
        String transactionType,

        @JsonProperty("content-type")
        String contentType
) {

    KisWebSocketSubscribeHeader {
        Objects.requireNonNull(approvalKey, "KIS WebSocket approval key는 필수입니다.");
        Objects.requireNonNull(customerType, "KIS 고객 타입은 필수입니다.");
        Objects.requireNonNull(transactionType, "KIS WebSocket 거래 타입은 필수입니다.");
        Objects.requireNonNull(contentType, "KIS WebSocket 메시지 인코딩 타입은 필수입니다.");
    }
}
