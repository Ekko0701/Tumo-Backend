package com.tumo.stock.port.handler;

import com.tumo.stock.domain.orderbook.StockOrderBookEvent;

/**
 * 실시간 호가 이벤트를 처리하는 callback interface.
 */
@FunctionalInterface
public interface StockOrderBookEventHandler {

    /**
     * 수신한 호가 이벤트를 처리한다.
     *
     * @param event 처리할 호가 이벤트
     */
    void handle(StockOrderBookEvent event);
}
