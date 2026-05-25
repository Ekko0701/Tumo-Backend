package com.tumo.stock.service;

import com.tumo.stock.domain.orderbook.StockOrderBookEvent;
import com.tumo.stock.port.StockOrderBookPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 실시간 호가 이벤트를 처리해 구독자에게 전달하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class StockOrderBookService {

    private final StockOrderBookPublisher stockOrderBookPublisher;

    /**
     * 수신한 호가 이벤트를 처리한다.
     *
     * @param event 처리할 호가 이벤트
     */
    public void handle(StockOrderBookEvent event) {
        stockOrderBookPublisher.publish(event);
    }
}
