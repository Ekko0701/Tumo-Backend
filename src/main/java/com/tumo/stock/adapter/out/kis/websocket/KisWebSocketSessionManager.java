package com.tumo.stock.adapter.out.kis.websocket;

import com.tumo.stock.adapter.out.kis.config.KisProperties;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import lombok.extern.slf4j.Slf4j;

/**
 * KIS WebSocket 연결 세션을 생성하고 재사용하는 manager.
 */
@Slf4j
public class KisWebSocketSessionManager {

    /**
     * KIS Open API 연동에 필요한 설정 값.
     */
    private final KisProperties properties;

    /**
     * WebSocket 연결을 생성하는 JDK HTTP client.
     */
    private final HttpClient httpClient;

    /**
     * 현재 사용 중인 KIS WebSocket 연결.
     */
    private volatile WebSocket webSocket;

    /**
     * KIS WebSocket 세션 manager를 생성한다.
     *
     * @param properties KIS Open API 연동 설정 값
     */
    public KisWebSocketSessionManager(KisProperties properties) {
        this(properties, HttpClient.newHttpClient());
    }

    /**
     * KIS WebSocket 세션 manager를 생성한다.
     *
     * @param properties KIS Open API 연동 설정 값
     * @param httpClient WebSocket 연결을 생성할 HTTP client
     */
    KisWebSocketSessionManager(KisProperties properties, HttpClient httpClient) {
        this.properties = Objects.requireNonNull(properties, "KIS 설정 값은 필수입니다.");
        this.httpClient = Objects.requireNonNull(httpClient, "HTTP client는 필수입니다.");
    }

    /**
     * KIS WebSocket 연결을 생성하거나 기존 연결을 반환한다.
     *
     * @return KIS WebSocket 연결
     */
    public synchronized WebSocket connect() {
        if (webSocket != null) {
            return webSocket;
        }

        try {
            webSocket = httpClient.newWebSocketBuilder()
                    .buildAsync(webSocketUri(), new KISWebSocketListener())
                    .join();
            return webSocket;
        } catch (CompletionException exception) {
            throw new IllegalStateException("KIS WebSocket 연결에 실패했습니다.", exception);
        }
    }

    private URI webSocketUri() {
        String websocketUrl = properties.websocketUrl();
        String websocketPath = properties.websocketPath();

        if (websocketUrl == null || websocketUrl.isBlank()) {
            throw new IllegalStateException("KIS WebSocket URL이 설정되지 않았습니다.");
        }

        if (websocketPath == null || websocketPath.isBlank()) {
            throw new IllegalStateException("KIS WebSocket 경로가 설정되지 않았습니다.");
        }

        return URI.create(websocketUrl + websocketPath);
    }

    private static class KISWebSocketListener implements WebSocket.Listener {

        @Override
        public void onOpen(WebSocket webSocket) {
            log.info("KIS WebSocket 연결이 열렸습니다.");
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            log.debug("KIS WebSocket 메시지를 수신했습니다.");
            webSocket.request(1);
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }
    }
}
