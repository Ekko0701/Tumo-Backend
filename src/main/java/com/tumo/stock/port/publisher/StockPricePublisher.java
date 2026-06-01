package com.tumo.stock.port.publisher;

import com.tumo.stock.domain.price.StockPriceEvent;

/**
 * 처리된 가격 이벤트를 Backend 외부 구독자에게 전달하는 outbound port.
 */
public interface StockPricePublisher {

    /**
     * 가격 이벤트를 구독자에게 발행한다.
     *
     * @param event 발행할 가격 이벤트
     */
    void publish(StockPriceEvent event);
}
