package com.tumo.stock.adapter.out.sse;

import static org.assertj.core.api.Assertions.assertThat;

import com.tumo.stock.domain.orderbook.StockOrderBook;
import com.tumo.stock.domain.orderbook.StockOrderBookEvent;
import com.tumo.stock.domain.orderbook.StockOrderBookLevel;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class StockOrderBookSseEmitterRegistryTest {

    @Test
    void publishSendsEventToMatchedStockCodeSubscriber() {
        RecordingSseEmitter samsungEmitter = new RecordingSseEmitter();
        RecordingSseEmitter skHynixEmitter = new RecordingSseEmitter();
        TestStockOrderBookSseEmitterRegistry registry = new TestStockOrderBookSseEmitterRegistry(
                samsungEmitter,
                skHynixEmitter
        );
        registry.connect("005930");
        registry.connect("000660");

        registry.publish(event("005930"));

        assertThat(samsungEmitter.sendCount).isEqualTo(1);
        assertThat(skHynixEmitter.sendCount).isZero();
    }

    private StockOrderBookEvent event(String stockCode) {
        StockOrderBook orderBook = new StockOrderBook(
                stockCode,
                List.of(new StockOrderBookLevel(75100L, 100L)),
                List.of(new StockOrderBookLevel(75000L, 120L)),
                LocalDateTime.of(2026, 5, 28, 9, 0)
        );
        return StockOrderBookEvent.fromKis(orderBook, LocalDateTime.of(2026, 5, 28, 9, 0, 1));
    }

    private static class TestStockOrderBookSseEmitterRegistry extends StockOrderBookSseEmitterRegistry {

        private final Queue<SseEmitter> emitters;

        TestStockOrderBookSseEmitterRegistry(SseEmitter... emitters) {
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
