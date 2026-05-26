package com.tumo.stock.adapter.out.kis.websocket.message;

import java.util.Objects;

/**
 * KIS WebSocket 구독 요청 body.
 *
 * @param input KIS WebSocket 구독 요청 입력 값
 */
record KisWebSocketSubscribeBody(
        KisWebSocketSubscribeInput input
) {

    KisWebSocketSubscribeBody {
        Objects.requireNonNull(input, "KIS WebSocket 구독 요청 입력 값은 필수입니다.");
    }
}
