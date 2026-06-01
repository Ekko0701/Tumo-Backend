package com.tumo.stock.adapter.out.kis.websocket.client;

import com.tumo.stock.adapter.out.kis.websocket.auth.KisApprovalKeyClient;
import com.tumo.stock.adapter.out.kis.config.KisProperties;
import com.tumo.stock.adapter.out.kis.websocket.dispatcher.KisWebSocketMessageDispatcher;
import com.tumo.stock.adapter.out.kis.websocket.message.KisWebSocketMessageSender;
import com.tumo.stock.adapter.out.kis.websocket.message.KisWebSocketSubscribeMessage;
import com.tumo.stock.adapter.out.kis.websocket.session.KisWebSocketSessionManager;
import com.tumo.stock.port.handler.StockOrderBookEventHandler;
import com.tumo.stock.port.handler.StockPriceEventHandler;
import com.tumo.stock.port.client.StockRealtimeOrderBookClient;
import com.tumo.stock.port.client.StockRealtimePriceClient;
import java.net.http.WebSocket;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * KIS WebSocket 기반 실시간 체결가와 실시간 호가 outbound adapter.
 */
public class KisRealtimeWebSocketClient implements StockRealtimePriceClient, StockRealtimeOrderBookClient {

    /**
     * KIS WebSocket approval key를 발급받는 client.
     */
    private final KisApprovalKeyClient approvalKeyClient;

    /**
     * KIS Open API 연동에 필요한 설정 값.
     */
    private final KisProperties properties;

    /**
     * KIS WebSocket 연결을 관리하는 manager.
     */
    private final KisWebSocketSessionManager sessionManager;

    /**
     * KIS WebSocket 메시지를 전송하는 sender.
     */
    private final KisWebSocketMessageSender messageSender;

    /**
     * KIS WebSocket 수신 메시지를 체결가/호가 이벤트 처리 흐름으로 분배하는 dispatcher.
     */
    private final KisWebSocketMessageDispatcher messageDispatcher;

    /**
     * KIS 체결가 메시지를 수신했을 때 호출할 handler.
     */
    private volatile StockPriceEventHandler priceEventHandler;

    /**
     * KIS 호가 메시지를 수신했을 때 호출할 handler.
     */
    private volatile StockOrderBookEventHandler orderBookEventHandler;

    /**
     * KIS WebSocket client를 생성한다.
     *
     * @param approvalKeyClient KIS WebSocket approval key 발급 client
     * @param properties KIS Open API 연동 설정 값
     * @param sessionManager KIS WebSocket 연결 manager
     * @param messageSender KIS WebSocket 메시지 sender
     * @param messageDispatcher KIS WebSocket 수신 메시지 dispatcher
     */
    public KisRealtimeWebSocketClient(
            KisApprovalKeyClient approvalKeyClient,
            KisProperties properties,
            KisWebSocketSessionManager sessionManager,
            KisWebSocketMessageSender messageSender,
            KisWebSocketMessageDispatcher messageDispatcher
    ) {
        this.approvalKeyClient = Objects.requireNonNull(approvalKeyClient, "KIS approval key client는 필수입니다.");
        this.properties = Objects.requireNonNull(properties, "KIS 설정 값은 필수입니다.");
        this.sessionManager = Objects.requireNonNull(sessionManager, "KIS WebSocket session manager는 필수입니다.");
        this.messageSender = Objects.requireNonNull(messageSender, "KIS WebSocket message sender는 필수입니다.");
        this.messageDispatcher = Objects.requireNonNull(messageDispatcher, "KIS WebSocket message dispatcher는 필수입니다.");
    }

    /**
     * 여러 종목의 KIS 실시간체결가를 구독한다.
     *
     * @param stockCodes 실시간 가격 이벤트를 구독할 종목 코드 목록
     * @param handler 수신한 가격 이벤트를 전달할 handler
     */
    @Override
    public void subscribe(Collection<String> stockCodes, StockPriceEventHandler handler) {
        this.priceEventHandler = Objects.requireNonNull(handler, "실시간 가격 이벤트 handler는 필수입니다.");
        List<String> normalizedStockCodes = normalizeStockCodes(stockCodes);

        if (normalizedStockCodes.isEmpty()) {
            return;
        }

        String approvalKey = approvalKeyClient.issueApprovalKey();
        WebSocket webSocket = sessionManager.connect(this::handleRawMessage);

        normalizedStockCodes.stream()
                .map(stockCode -> KisWebSocketSubscribeMessage.subscribeTradePrice(approvalKey, properties, stockCode))
                .forEach(message -> messageSender.send(webSocket, message));
    }

    /**
     * 여러 종목의 KIS 실시간호가를 구독한다.
     *
     * @param stockCodes 실시간 호가 이벤트를 구독할 종목 코드 목록
     * @param handler 수신한 호가 이벤트를 전달할 handler
     */
    @Override
    public void subscribe(Collection<String> stockCodes, StockOrderBookEventHandler handler) {
        this.orderBookEventHandler = Objects.requireNonNull(handler, "실시간 호가 이벤트 handler는 필수입니다.");
        List<String> normalizedStockCodes = normalizeStockCodes(stockCodes);

        if (normalizedStockCodes.isEmpty()) {
            return;
        }

        String approvalKey = approvalKeyClient.issueApprovalKey();
        WebSocket webSocket = sessionManager.connect(this::handleRawMessage);

        normalizedStockCodes.stream()
                .map(stockCode -> KisWebSocketSubscribeMessage.subscribeOrderBook(approvalKey, properties, stockCode))
                .forEach(message -> messageSender.send(webSocket, message));
    }

    /**
     * 여러 종목의 KIS 실시간체결가와 실시간호가 구독을 해제한다.
     *
     * @param stockCodes 구독을 해제할 종목 코드 목록
     */
    @Override
    public void unsubscribe(Collection<String> stockCodes) {
        List<String> normalizedStockCodes = normalizeStockCodes(stockCodes);

        if (normalizedStockCodes.isEmpty()) {
            return;
        }

        String approvalKey = approvalKeyClient.issueApprovalKey();
        WebSocket webSocket = sessionManager.connect(this::handleRawMessage);

        normalizedStockCodes.forEach(stockCode -> {
            messageSender.send(webSocket, KisWebSocketSubscribeMessage.unsubscribeTradePrice(approvalKey, properties, stockCode));
            messageSender.send(webSocket, KisWebSocketSubscribeMessage.unsubscribeOrderBook(approvalKey, properties, stockCode));
        });
    }

    /**
     * KIS WebSocket에서 수신한 raw message를 dispatcher에 전달한다.
     *
     * @param rawMessage KIS WebSocket에서 수신한 원본 메시지
     */
    void handleRawMessage(String rawMessage) {
        messageDispatcher.dispatch(rawMessage, priceEventHandler, orderBookEventHandler);
    }

    private List<String> normalizeStockCodes(Collection<String> stockCodes) {
        Objects.requireNonNull(stockCodes, "종목 코드 목록은 필수입니다.");

        return stockCodes.stream()
                .map(stockCode -> {
                    if (stockCode == null || stockCode.isBlank()) {
                        throw new IllegalArgumentException("종목 코드는 비어 있을 수 없습니다.");
                    }
                    return stockCode;
                })
                .toList();
    }
}
