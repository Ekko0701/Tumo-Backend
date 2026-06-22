package com.tumo.stock.port.client;

import com.tumo.stock.port.handler.StockOrderBookEventHandler;
import java.util.Collection;

/**
 * 외부 실시간 시세 provider에서 호가 이벤트를 구독하는 outbound port.
 */
public interface StockRealtimeOrderBookClient {

    /**
     * 여러 종목의 실시간 호가 이벤트를 구독한다.
     *
     * @param stockCodes 실시간 호가 이벤트를 구독할 종목 코드 목록
     * @param handler 수신한 호가 이벤트를 전달할 handler
     */
    void subscribe(Collection<String> stockCodes, StockOrderBookEventHandler handler);

    /**
     * 여러 종목의 실시간 호가 이벤트 구독을 해제한다.
     *
     * @param stockCodes 구독을 해제할 종목 코드 목록
     */
    void unsubscribeOrderBook(Collection<String> stockCodes);
}
