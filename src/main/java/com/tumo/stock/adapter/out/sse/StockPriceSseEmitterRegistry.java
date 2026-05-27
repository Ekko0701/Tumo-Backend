package com.tumo.stock.adapter.out.sse;

import com.tumo.stock.domain.price.StockPriceEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
     * 실시간 가격 이벤트를 수신할 SSE 구독 목록.
     */
    private final List<StockPriceSseSubscription> subscriptions = new CopyOnWriteArrayList<>();

    /**
     * 모든 종목의 실시간 가격 이벤트를 수신할 SSE 연결을 등록한다.
     *
     * @return 등록된 SSE 연결
     */
    public SseEmitter connect() {
        return connect(List.of());
    }

    /**
     * 지정한 종목의 실시간 가격 이벤트를 수신할 SSE 연결을 등록한다.
     *
     * @param stockCodes 실시간 가격 이벤트를 수신할 종목 코드 목록
     * @return 등록된 SSE 연결
     */
    public SseEmitter connect(Collection<String> stockCodes) {
        Set<String> subscribedStockCodes = normalizeStockCodes(stockCodes);
        SseEmitter emitter = createEmitter();
        StockPriceSseSubscription subscription = new StockPriceSseSubscription(emitter, subscribedStockCodes);

        subscriptions.add(subscription);
        emitter.onCompletion(() -> subscriptions.remove(subscription));
        emitter.onTimeout(() -> subscriptions.remove(subscription));
        emitter.onError(error -> subscriptions.remove(subscription));
        return emitter;
    }

    /**
     * 연결된 모든 SSE 구독자에게 실시간 가격 이벤트를 전송한다.
     *
     * @param event 전송할 실시간 가격 이벤트
     */
    public void publish(StockPriceEvent event) {
        Objects.requireNonNull(event, "가격 이벤트는 필수입니다.");
        subscriptions.stream()
                .filter(subscription -> subscription.matches(event.price().stockCode()))
                .forEach(subscription -> send(subscription, event));
    }

    protected SseEmitter createEmitter() {
        return new SseEmitter(0L);
    }

    private void send(StockPriceSseSubscription subscription, StockPriceEvent event) {
        try {
            subscription.emitter().send(SseEmitter.event()
                    .name(STOCK_PRICE_EVENT_NAME)
                    .data(event));
        } catch (IOException | IllegalStateException exception) {
            subscriptions.remove(subscription);
            log.debug("실시간 가격 SSE 이벤트 전송에 실패해 연결을 제거했습니다.", exception);
        }
    }

    private Set<String> normalizeStockCodes(Collection<String> stockCodes) {
        if (stockCodes == null || stockCodes.isEmpty()) {
            return Set.of();
        }

        Set<String> normalizedStockCodes = new LinkedHashSet<>();
        stockCodes.stream()
                .filter(Objects::nonNull)
                .flatMap(stockCode -> Arrays.stream(stockCode.split(",")))
                .map(String::trim)
                .filter(stockCode -> !stockCode.isBlank())
                .forEach(normalizedStockCodes::add);
        return Set.copyOf(normalizedStockCodes);
    }

    /**
     * 실시간 가격 SSE 연결과 관심 종목 목록.
     *
     * @param emitter 실시간 가격 이벤트를 전송할 SSE 연결
     * @param stockCodes 이 연결이 수신할 종목 코드 목록
     */
    private record StockPriceSseSubscription(
            SseEmitter emitter,
            Set<String> stockCodes
    ) {

        private boolean matches(String stockCode) {
            return stockCodes.isEmpty() || stockCodes.contains(stockCode);
        }
    }
}
