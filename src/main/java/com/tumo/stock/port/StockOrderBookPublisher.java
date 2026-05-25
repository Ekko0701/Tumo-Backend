package com.tumo.stock.port;

import com.tumo.stock.domain.orderbook.StockOrderBookEvent;

/**
 * 처리된 호가 이벤트를 Backend 외부 구독자에게 전달하는 outbound port.
 */
public interface StockOrderBookPublisher {

    /**
     * 호가 이벤트를 구독자에게 발행한다.
     *
     * @param event 발행할 호가 이벤트
     */
    void publish(StockOrderBookEvent event);
}
