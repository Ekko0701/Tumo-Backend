package com.tumo.stock.adapter.out.sse;

import com.tumo.stock.domain.price.StockPriceEvent;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 실시간 가격 SSE 연결과 이벤트 전송을 관리하는 registry.
 */
@Slf4j
@Component
public class StockPriceSseEmitterRegistry {

    private static final String STOCK_PRICE_EVENT_NAME = "stock-price";

    /**
     * 실시간 가격 이벤트를 수신할 SSE 연결 목록.
     */
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * 실시간 가격 이벤트를 수신할 SSE 연결을 등록한다.
     *
     * @return 등록된 SSE 연결
     */
    public SseEmitter connect() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
        return emitter;
    }

    /**
     * 연결된 모든 SSE 구독자에게 실시간 가격 이벤트를 전송한다.
     *
     * @param event 전송할 실시간 가격 이벤트
     */
    public void publish(StockPriceEvent event) {
        Objects.requireNonNull(event, "가격 이벤트는 필수입니다.");
        emitters.forEach(emitter -> send(emitter, event));
    }

    private void send(SseEmitter emitter, StockPriceEvent event) {
        try {
            emitter.send(SseEmitter.event()
                    .name(STOCK_PRICE_EVENT_NAME)
                    .data(event));
        } catch (IOException | IllegalStateException exception) {
            emitters.remove(emitter);
            log.debug("실시간 가격 SSE 이벤트 전송에 실패해 연결을 제거했습니다.", exception);
        }
    }
}
