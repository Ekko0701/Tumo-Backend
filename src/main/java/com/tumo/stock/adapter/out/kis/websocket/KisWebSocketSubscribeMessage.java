package com.tumo.stock.adapter.out.kis.websocket;

import com.tumo.stock.adapter.out.kis.config.KisProperties;
import java.util.Objects;

/**
 * KIS WebSocket 실시간 데이터 구독 요청 메시지.
 *
 * @param header KIS WebSocket 구독 요청 header
 * @param body KIS WebSocket 구독 요청 body
 */
public record KisWebSocketSubscribeMessage(
        KisWebSocketSubscribeHeader header,
        KisWebSocketSubscribeBody body
) {

    private static final String SUBSCRIBE_TRANSACTION_TYPE = "1";

    private static final String CONTENT_TYPE = "utf-8";

    public KisWebSocketSubscribeMessage {
        Objects.requireNonNull(header, "KIS WebSocket 구독 요청 header는 필수입니다.");
        Objects.requireNonNull(body, "KIS WebSocket 구독 요청 body는 필수입니다.");
    }

    /**
     * 국내주식 실시간체결가 구독 요청 메시지를 생성한다.
     *
     * @param approvalKey KIS WebSocket approval key
     * @param properties KIS Open API 연동 설정 값
     * @param stockCode 구독할 종목 코드
     * @return 국내주식 실시간체결가 구독 요청 메시지
     */
    public static KisWebSocketSubscribeMessage subscribeTradePrice(
            String approvalKey,
            KisProperties properties,
            String stockCode
    ) {
        Objects.requireNonNull(properties, "KIS 설정 값은 필수입니다.");
        return subscribe(approvalKey, properties.customerType(), properties.tradePriceTrId(), stockCode);
    }

    /**
     * 국내주식 실시간호가 구독 요청 메시지를 생성한다.
     *
     * @param approvalKey KIS WebSocket approval key
     * @param properties KIS Open API 연동 설정 값
     * @param stockCode 구독할 종목 코드
     * @return 국내주식 실시간호가 구독 요청 메시지
     */
    public static KisWebSocketSubscribeMessage subscribeOrderBook(
            String approvalKey,
            KisProperties properties,
            String stockCode
    ) {
        Objects.requireNonNull(properties, "KIS 설정 값은 필수입니다.");
        return subscribe(approvalKey, properties.customerType(), properties.orderBookTrId(), stockCode);
    }

    private static KisWebSocketSubscribeMessage subscribe(
            String approvalKey,
            String customerType,
            String transactionId,
            String stockCode
    ) {
        KisWebSocketSubscribeHeader header = new KisWebSocketSubscribeHeader(
                approvalKey,
                customerType,
                SUBSCRIBE_TRANSACTION_TYPE,
                CONTENT_TYPE
        );
        KisWebSocketSubscribeInput input = new KisWebSocketSubscribeInput(transactionId, stockCode);
        KisWebSocketSubscribeBody body = new KisWebSocketSubscribeBody(input);

        return new KisWebSocketSubscribeMessage(header, body);
    }
}
