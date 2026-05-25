package com.tumo.stock.adapter.out.kis.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.WebSocket;
import java.util.Objects;
import java.util.concurrent.CompletionException;

/**
 * KIS WebSocket 구독 메시지를 JSON으로 변환해 WebSocket으로 전송하는 sender.
 */
public class KisWebSocketMessageSender {

    /**
     * KIS WebSocket 메시지를 JSON 문자열로 변환하는 mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * KIS WebSocket 메시지 sender를 생성한다.
     *
     * @param objectMapper JSON 직렬화 mapper
     */
    public KisWebSocketMessageSender(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper는 필수입니다.");
    }

    /**
     * KIS WebSocket 구독 메시지를 전송한다.
     *
     * @param webSocket KIS WebSocket 연결
     * @param message KIS WebSocket 구독 요청 메시지
     */
    public void send(WebSocket webSocket, KisWebSocketSubscribeMessage message) {
        Objects.requireNonNull(webSocket, "KIS WebSocket 연결은 필수입니다.");
        Objects.requireNonNull(message, "KIS WebSocket 구독 요청 메시지는 필수입니다.");

        try {
            String payload = objectMapper.writeValueAsString(message);
            webSocket.sendText(payload, true).join();
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("KIS WebSocket 구독 메시지 변환에 실패했습니다.", exception);
        } catch (CompletionException exception) {
            throw new IllegalStateException("KIS WebSocket 구독 메시지 전송에 실패했습니다.", exception);
        }
    }
}
