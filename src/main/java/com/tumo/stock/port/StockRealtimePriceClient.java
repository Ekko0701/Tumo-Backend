package com.tumo.stock.port;

import java.util.Collection;

/**
 * 외부 실시간 시세 provider에서 가격 이벤트를 구독하는 outbound port.
 */
public interface StockRealtimePriceClient {

    /**
     * 여러 종목의 실시간 가격 이벤트를 구독한다.
     *
     * @param stockCodes 실시간 가격 이벤트를 구독할 종목 코드 목록
     * @param handler 수신한 가격 이벤트를 전달할 handler
     */
    void subscribe(Collection<String> stockCodes, StockPriceEventHandler handler);

    /**
     * 여러 종목의 실시간 가격 이벤트 구독을 해제한다.
     *
     * @param stockCodes 구독을 해제할 종목 코드 목록
     */
    void unsubscribe(Collection<String> stockCodes);
}
