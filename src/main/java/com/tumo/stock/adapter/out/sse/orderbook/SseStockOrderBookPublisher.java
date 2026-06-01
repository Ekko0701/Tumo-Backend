package com.tumo.stock.adapter.out.sse.orderbook;

import com.tumo.stock.domain.orderbook.StockOrderBookEvent;
import com.tumo.stock.port.publisher.StockOrderBookPublisher;
import java.util.Objects;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 처리된 실시간 호가 이벤트를 SSE 구독자에게 전달하는 publisher adapter.
 */
@Primary
@Component
public class SseStockOrderBookPublisher implements StockOrderBookPublisher {

    /**
     * 실시간 호가 SSE 연결과 이벤트 전송을 관리하는 registry.
     */
    private final StockOrderBookSseEmitterRegistry emitterRegistry;

    /**
     * SSE 기반 실시간 호가 publisher를 생성한다.
     *
     * @param emitterRegistry 실시간 호가 SSE emitter registry
     */
    public SseStockOrderBookPublisher(StockOrderBookSseEmitterRegistry emitterRegistry) {
        this.emitterRegistry = Objects.requireNonNull(emitterRegistry, "실시간 호가 SSE emitter registry는 필수입니다.");
    }

    /**
     * 호가 이벤트를 SSE 구독자에게 발행한다.
     *
     * @param event 발행할 호가 이벤트
     */
    @Override
    public void publish(StockOrderBookEvent event) {
        emitterRegistry.publish(event);
    }
}
