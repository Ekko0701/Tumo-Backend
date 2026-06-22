package com.tumo.stock.adapter.out.sse.orderbook;

import com.tumo.stock.domain.orderbook.StockOrderBookEvent;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
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

    private static final String HEARTBEAT_EVENT_NAME = "heartbeat";

    private static final String HEARTBEAT_DATA = "ok";

    private static final long SSE_TIMEOUT_MILLIS = 30 * 60 * 1000L;

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
        return connect(stockCode, () -> { });
    }

    /**
     * 특정 종목의 실시간 호가 이벤트를 수신할 SSE 연결을 등록하고, 연결 종료 시 정리 작업을 실행한다.
     *
     * @param stockCode 실시간 호가 이벤트를 수신할 종목 코드
     * @param onRemove 연결이 종료(완료/타임아웃/에러)되어 제거될 때 한 번 실행할 정리 작업(구독 해제 등)
     * @return 등록된 SSE 연결
     */
    public SseEmitter connect(String stockCode, Runnable onRemove) {
        Objects.requireNonNull(onRemove, "SSE 연결 종료 정리 작업은 필수입니다.");

        String normalizedStockCode = normalizeStockCode(stockCode);
        SseEmitter emitter = createEmitter();
        StockOrderBookSseSubscription subscription = new StockOrderBookSseSubscription(emitter, normalizedStockCode);

        subscriptions.add(subscription);
        // 완료/타임아웃/에러 중 어떤 경로로 끝나든 정리를 정확히 한 번만 실행한다.
        Runnable cleanup = onceRunnable(() -> {
            subscriptions.remove(subscription);
            onRemove.run();
        });
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(error -> cleanup.run());
        return emitter;
    }

    private Runnable onceRunnable(Runnable action) {
        AtomicBoolean done = new AtomicBoolean(false);
        return () -> {
            if (done.compareAndSet(false, true)) {
                action.run();
            }
        };
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
                .forEach(subscription -> sendOrderBookEvent(subscription, event));
    }

    /**
     * 연결된 모든 SSE 구독자에게 heartbeat 이벤트를 전송한다.
     */
    public void sendHeartbeat() {
        subscriptions.forEach(this::sendHeartbeatEvent);
    }

    protected SseEmitter createEmitter() {
        return new SseEmitter(SSE_TIMEOUT_MILLIS);
    }

    private void sendOrderBookEvent(StockOrderBookSseSubscription subscription, StockOrderBookEvent event) {
        try {
            subscription.emitter().send(SseEmitter.event()
                    .name(STOCK_ORDER_BOOK_EVENT_NAME)
                    .data(event));
        } catch (IOException | IllegalStateException exception) {
            remove(subscription, "실시간 호가 SSE 이벤트 전송에 실패해 연결을 제거했습니다.", exception);
        }
    }

    private void sendHeartbeatEvent(StockOrderBookSseSubscription subscription) {
        try {
            subscription.emitter().send(SseEmitter.event()
                    .name(HEARTBEAT_EVENT_NAME)
                    .data(HEARTBEAT_DATA));
        } catch (IOException | IllegalStateException exception) {
            remove(subscription, "실시간 호가 SSE heartbeat 전송에 실패해 연결을 제거했습니다.", exception);
        }
    }

    private void remove(StockOrderBookSseSubscription subscription, String message, Exception exception) {
        subscriptions.remove(subscription);
        log.debug(message, exception);
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
