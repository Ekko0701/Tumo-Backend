package com.tumo.stock.adapter.out.sse.price;

import com.tumo.stock.domain.price.StockPriceEvent;
import com.tumo.stock.port.publisher.StockPricePublisher;
import java.util.Objects;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 처리된 실시간 가격 이벤트를 SSE 구독자에게 전달하는 publisher adapter.
 */
@Primary
@Component
public class SseStockPricePublisher implements StockPricePublisher {

    /**
     * 실시간 가격 SSE 연결과 이벤트 전송을 관리하는 registry.
     */
    private final StockPriceSseEmitterRegistry emitterRegistry;

    /**
     * SSE 기반 실시간 가격 publisher를 생성한다.
     *
     * @param emitterRegistry 실시간 가격 SSE emitter registry
     */
    public SseStockPricePublisher(StockPriceSseEmitterRegistry emitterRegistry) {
        this.emitterRegistry = Objects.requireNonNull(emitterRegistry, "실시간 가격 SSE emitter registry는 필수입니다.");
    }

    /**
     * 가격 이벤트를 SSE 구독자에게 발행한다.
     *
     * @param event 발행할 가격 이벤트
     */
    @Override
    public void publish(StockPriceEvent event) {
        emitterRegistry.publish(event);
    }
}
