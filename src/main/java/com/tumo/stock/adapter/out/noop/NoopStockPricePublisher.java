package com.tumo.stock.adapter.out.noop;

import com.tumo.stock.domain.price.StockPriceEvent;
import com.tumo.stock.port.publisher.StockPricePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 실시간 가격 이벤트 전송 구현이 붙기 전까지 이벤트를 로그로만 처리하는 기본 publisher adapter.
 */
@Slf4j
@Component
public class NoopStockPricePublisher implements StockPricePublisher {

    /**
     * 가격 이벤트를 외부 구독자에게 전송하지 않고 로그로만 남긴다.
     *
     * @param event 발행 요청을 받은 가격 이벤트
     */
    @Override
    public void publish(StockPriceEvent event) {
        log.debug("Stock price event publishing is not configured yet. event={}", event);
    }
}
