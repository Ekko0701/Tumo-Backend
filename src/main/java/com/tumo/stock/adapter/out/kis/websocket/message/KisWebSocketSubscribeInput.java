package com.tumo.stock.adapter.out.kis.websocket.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * KIS WebSocket 구독 요청 입력 값.
 *
 * @param transactionId KIS 실시간 데이터 TR ID
 * @param stockCode 구독할 종목 코드
 */
record KisWebSocketSubscribeInput(
        @JsonProperty("tr_id")
        String transactionId,

        @JsonProperty("tr_key")
        String stockCode
) {

    KisWebSocketSubscribeInput {
        Objects.requireNonNull(transactionId, "KIS 실시간 데이터 TR ID는 필수입니다.");
        Objects.requireNonNull(stockCode, "구독할 종목 코드는 필수입니다.");
    }
}
