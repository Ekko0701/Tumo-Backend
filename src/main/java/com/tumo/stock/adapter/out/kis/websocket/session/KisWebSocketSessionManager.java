package com.tumo.stock.adapter.out.kis.websocket.session;

import com.tumo.stock.adapter.out.kis.config.KisProperties;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
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
     * KIS WebSocket에서 수신한 raw message를 처리하는 callback.
     */
    private volatile Consumer<String> messageHandler = message -> {
    };

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
        return connect(messageHandler);
    }

    /**
     * KIS WebSocket 연결을 생성하거나 기존 연결을 반환하고 수신 메시지 callback을 등록한다.
     *
     * @param messageHandler KIS WebSocket에서 수신한 raw message를 처리하는 callback
     * @return KIS WebSocket 연결
     */
    public synchronized WebSocket connect(Consumer<String> messageHandler) {
        this.messageHandler = Objects.requireNonNull(messageHandler, "KIS WebSocket message handler는 필수입니다.");

        if (isConnected(webSocket)) {
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

    private boolean isConnected(WebSocket webSocket) {
        return webSocket != null
                && !webSocket.isInputClosed()
                && !webSocket.isOutputClosed();
    }

    private synchronized void clearClosedWebSocket(WebSocket closedWebSocket) {
        if (webSocket == closedWebSocket) {
            webSocket = null;
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

    private class KISWebSocketListener implements WebSocket.Listener {

        /**
         * WebSocket text frame이 분할되어 들어올 때 메시지를 합치기 위한 buffer.
         */
        private final StringBuilder textBuffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            log.info("KIS WebSocket 연결이 열렸습니다.");
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            log.debug("KIS WebSocket 메시지를 수신했습니다.");
            handleText(data, last);
            webSocket.request(1);
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            log.warn("KIS WebSocket 연결이 닫혔습니다. statusCode={}, reason={}", statusCode, reason);
            clearClosedWebSocket(webSocket);
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            log.warn("KIS WebSocket 연결에서 오류가 발생했습니다.", error);
            clearClosedWebSocket(webSocket);
            WebSocket.Listener.super.onError(webSocket, error);
        }

        /**
         * 수신한 text frame을 완성된 raw message로 만든 뒤 callback에 전달한다.
         *
         * @param data 수신한 text frame
         * @param last 현재 frame이 메시지의 마지막 frame인지 여부
         */
        private void handleText(CharSequence data, boolean last) {
            textBuffer.append(data);

            if (!last) {
                return;
            }

            String rawMessage = textBuffer.toString();
            textBuffer.setLength(0);

            try {
                messageHandler.accept(rawMessage);
            } catch (RuntimeException exception) {
                log.warn("KIS WebSocket 수신 메시지 callback 처리에 실패했습니다.", exception);
            }
        }
    }
}
