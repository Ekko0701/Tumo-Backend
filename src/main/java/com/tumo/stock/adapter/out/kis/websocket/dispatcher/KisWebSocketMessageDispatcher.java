package com.tumo.stock.adapter.out.kis.websocket.dispatcher;

import com.tumo.stock.adapter.out.kis.config.KisProperties;
import com.tumo.stock.adapter.out.kis.websocket.parser.KisOrderBookMessageParser;
import com.tumo.stock.adapter.out.kis.websocket.parser.KisTradePriceMessageParser;
import com.tumo.stock.domain.orderbook.StockOrderBookEvent;
import com.tumo.stock.domain.price.StockPriceEvent;
import com.tumo.stock.port.handler.StockOrderBookEventHandler;
import com.tumo.stock.port.handler.StockPriceEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * KIS WebSocket 수신 메시지를 체결가/호가 처리 흐름으로 분배하는 dispatcher.
 */
@Slf4j
@RequiredArgsConstructor
public class KisWebSocketMessageDispatcher {

    /**
     * KIS Open API 연동에 필요한 설정 값.
     */
    private final KisProperties properties;

    /**
     * KIS 실시간체결가 메시지를 가격 이벤트로 변환하는 parser.
     */
    private final KisTradePriceMessageParser tradePriceMessageParser;

    /**
     * KIS 실시간호가 메시지를 호가 이벤트로 변환하는 parser.
     */
    private final KisOrderBookMessageParser orderBookMessageParser;

    /**
     * KIS raw message를 메시지 종류에 맞는 handler로 전달한다.
     *
     * @param rawMessage KIS WebSocket에서 수신한 원본 메시지
     * @param priceEventHandler 체결가 이벤트를 처리할 handler
     * @param orderBookEventHandler 호가 이벤트를 처리할 handler
     */
    public void dispatch(
            String rawMessage,
            StockPriceEventHandler priceEventHandler,
            StockOrderBookEventHandler orderBookEventHandler
    ) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return;
        }

        if (isControlMessage(rawMessage)) {
            log.debug("KIS WebSocket 제어 메시지를 수신했습니다.");
            return;
        }

        try {
            if (rawMessage.contains(properties.tradePriceTrId())) {
                dispatchTradePrice(rawMessage, priceEventHandler);
                return;
            }

            if (rawMessage.contains(properties.orderBookTrId())) {
                dispatchOrderBook(rawMessage, orderBookEventHandler);
                return;
            }

            log.debug("처리 대상이 아닌 KIS WebSocket 메시지를 수신했습니다.");
        } catch (RuntimeException exception) {
            log.warn("KIS WebSocket 메시지 처리에 실패했습니다.", exception);
        }
    }

    /**
     * WebSocket 제어 메시지 여부를 확인한다.
     *
     * <p>KIS WebSocket은 구독 결과, ping/pong, 오류 응답처럼 JSON 형태의 제어 메시지를 보낼 수 있다.
     * 제어 메시지는 체결가/호가 이벤트가 아니므로 parser로 전달하지 않는다.</p>
     *
     * @param rawMessage KIS WebSocket에서 수신한 원본 메시지
     * @return 제어 메시지이면 true
     */
    private boolean isControlMessage(String rawMessage) {
        return rawMessage.startsWith("{") || rawMessage.startsWith("[");
    }

    /**
     * 체결가 메시지를 가격 이벤트로 변환하고 handler에 전달한다.
     *
     * @param rawMessage KIS 실시간체결가 원본 메시지
     * @param priceEventHandler 체결가 이벤트를 처리할 handler
     */
    private void dispatchTradePrice(
            String rawMessage,
            StockPriceEventHandler priceEventHandler
    ) {
        if (priceEventHandler == null) {
            return;
        }

        StockPriceEvent event = tradePriceMessageParser.parse(rawMessage);
        priceEventHandler.handle(event);
    }

    /**
     * 호가 메시지를 호가 이벤트로 변환하고 handler에 전달한다.
     *
     * @param rawMessage KIS 실시간호가 원본 메시지
     * @param orderBookEventHandler 호가 이벤트를 처리할 handler
     */
    private void dispatchOrderBook(
            String rawMessage,
            StockOrderBookEventHandler orderBookEventHandler
    ) {
        if (orderBookEventHandler == null) {
            return;
        }

        StockOrderBookEvent event = orderBookMessageParser.parse(rawMessage);
        orderBookEventHandler.handle(event);
    }
}
