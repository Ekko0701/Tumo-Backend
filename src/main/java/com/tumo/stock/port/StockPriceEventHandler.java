package com.tumo.stock.port;

import com.tumo.stock.domain.StockPriceEvent;

/**
 * 실시간 가격 이벤트를 처리하는 callback interface.
 */
@FunctionalInterface
public interface StockPriceEventHandler {

    /**
     * 수신한 가격 이벤트를 처리한다.
     *
     * @param event 처리할 가격 이벤트
     */
    void handle(StockPriceEvent event);
}
