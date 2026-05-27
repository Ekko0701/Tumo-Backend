package com.tumo.stock.adapter.out.sse;

import static org.assertj.core.api.Assertions.assertThat;

import com.tumo.stock.domain.price.StockPrice;
import com.tumo.stock.domain.price.StockPriceEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class StockPriceSseEmitterRegistryTest {

    @Test
    void publishSendsEventToAllSubscriberAndMatchedStockCodeSubscriber() {
        RecordingSseEmitter allStockEmitter = new RecordingSseEmitter();
        RecordingSseEmitter samsungEmitter = new RecordingSseEmitter();
        RecordingSseEmitter skHynixEmitter = new RecordingSseEmitter();
        TestStockPriceSseEmitterRegistry registry = new TestStockPriceSseEmitterRegistry(
                allStockEmitter,
                samsungEmitter,
                skHynixEmitter
        );
        registry.connect();
        registry.connect(List.of("005930"));
        registry.connect(List.of("000660"));

        registry.publish(event("005930"));

        assertThat(allStockEmitter.sendCount).isEqualTo(1);
        assertThat(samsungEmitter.sendCount).isEqualTo(1);
        assertThat(skHynixEmitter.sendCount).isZero();
    }

    @Test
    void sendHeartbeatSendsEventToAllSubscribers() {
        RecordingSseEmitter allStockEmitter = new RecordingSseEmitter();
        RecordingSseEmitter samsungEmitter = new RecordingSseEmitter();
        TestStockPriceSseEmitterRegistry registry = new TestStockPriceSseEmitterRegistry(
                allStockEmitter,
                samsungEmitter
        );
        registry.connect();
        registry.connect(List.of("005930"));

        registry.sendHeartbeat();

        assertThat(allStockEmitter.sendCount).isEqualTo(1);
        assertThat(samsungEmitter.sendCount).isEqualTo(1);
    }

    private StockPriceEvent event(String stockCode) {
        StockPrice price = new StockPrice(
                stockCode,
                75000L,
                1000L,
                BigDecimal.valueOf(1.35),
                1000000L,
                LocalDateTime.of(2026, 5, 28, 9, 0)
        );
        return StockPriceEvent.fromKis(price, LocalDateTime.of(2026, 5, 28, 9, 0, 1));
    }

    private static class TestStockPriceSseEmitterRegistry extends StockPriceSseEmitterRegistry {

        private final Queue<SseEmitter> emitters;

        TestStockPriceSseEmitterRegistry(SseEmitter... emitters) {
            this.emitters = new ArrayDeque<>(List.of(emitters));
        }

        @Override
        protected SseEmitter createEmitter() {
            return emitters.remove();
        }
    }

    private static class RecordingSseEmitter extends SseEmitter {

        private int sendCount;

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            sendCount++;
        }
    }
}
