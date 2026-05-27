package com.tumo.stock.adapter.out.sse;

import com.tumo.stock.domain.orderbook.StockOrderBookEvent;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 실시간 호가 SSE 연결과 이벤트 전송을 관리하는 registry.
 */
@Slf4j
@Component
public class StockOrderBookSseEmitterRegistry {

    private static final String STOCK_ORDER_BOOK_EVENT_NAME = "stock-order-book";

    /**
     * 실시간 호가 이벤트를 수신할 SSE 구독 목록.
     */
    private final List<StockOrderBookSseSubscription> subscriptions = new CopyOnWriteArrayList<>();

    /**
     * 특정 종목의 실시간 호가 이벤트를 수신할 SSE 연결을 등록한다.
     *
     * @param stockCode 실시간 호가 이벤트를 수신할 종목 코드
     * @return 등록된 SSE 연결
     */
    public SseEmitter connect(String stockCode) {
        String normalizedStockCode = normalizeStockCode(stockCode);
        SseEmitter emitter = createEmitter();
        StockOrderBookSseSubscription subscription = new StockOrderBookSseSubscription(emitter, normalizedStockCode);

        subscriptions.add(subscription);
        emitter.onCompletion(() -> subscriptions.remove(subscription));
        emitter.onTimeout(() -> subscriptions.remove(subscription));
        emitter.onError(error -> subscriptions.remove(subscription));
        return emitter;
    }

    /**
     * 연결된 모든 SSE 구독자에게 실시간 호가 이벤트를 전송한다.
     *
     * @param event 전송할 실시간 호가 이벤트
     */
    public void publish(StockOrderBookEvent event) {
        Objects.requireNonNull(event, "호가 이벤트는 필수입니다.");
        subscriptions.stream()
                .filter(subscription -> subscription.matches(event.orderBook().stockCode()))
                .forEach(subscription -> send(subscription, event));
    }

    protected SseEmitter createEmitter() {
        return new SseEmitter(0L);
    }

    private void send(StockOrderBookSseSubscription subscription, StockOrderBookEvent event) {
        try {
            subscription.emitter().send(SseEmitter.event()
                    .name(STOCK_ORDER_BOOK_EVENT_NAME)
                    .data(event));
        } catch (IOException | IllegalStateException exception) {
            subscriptions.remove(subscription);
            log.debug("실시간 호가 SSE 이벤트 전송에 실패해 연결을 제거했습니다.", exception);
        }
    }

    private String normalizeStockCode(String stockCode) {
        if (stockCode == null || stockCode.isBlank()) {
            throw new IllegalArgumentException("호가 stream을 구독할 종목 코드는 필수입니다.");
        }
        return stockCode.trim();
    }

    /**
     * 실시간 호가 SSE 연결과 관심 종목 코드.
     *
     * @param emitter 실시간 호가 이벤트를 전송할 SSE 연결
     * @param stockCode 이 연결이 수신할 종목 코드
     */
    private record StockOrderBookSseSubscription(
            SseEmitter emitter,
            String stockCode
    ) {

        private boolean matches(String eventStockCode) {
            return stockCode.equals(eventStockCode);
        }
    }
}
