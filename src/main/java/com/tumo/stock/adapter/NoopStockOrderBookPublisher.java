package com.tumo.stock.adapter;

import com.tumo.stock.domain.orderbook.StockOrderBookEvent;
import com.tumo.stock.port.StockOrderBookPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 실시간 호가 이벤트 전송 구현이 붙기 전까지 이벤트를 로그로만 처리하는 기본 publisher adapter.
 */
@Slf4j
@Component
public class NoopStockOrderBookPublisher implements StockOrderBookPublisher {

    /**
     * 호가 이벤트를 외부 구독자에게 전송하지 않고 로그로만 남긴다.
     *
     * @param event 발행 요청을 받은 호가 이벤트
     */
    @Override
    public void publish(StockOrderBookEvent event) {
        log.debug("Stock order book event publishing is not configured yet. event={}", event);
    }
}
