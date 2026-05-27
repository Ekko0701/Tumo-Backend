package com.tumo.stock.adapter.out.sse;

import static org.assertj.core.api.Assertions.assertThat;

import com.tumo.stock.domain.orderbook.StockOrderBook;
import com.tumo.stock.domain.orderbook.StockOrderBookEvent;
import com.tumo.stock.domain.orderbook.StockOrderBookLevel;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class SseStockOrderBookPublisherTest {

    @Test
    void publish() {
        FakeStockOrderBookSseEmitterRegistry emitterRegistry = new FakeStockOrderBookSseEmitterRegistry();
        SseStockOrderBookPublisher publisher = new SseStockOrderBookPublisher(emitterRegistry);
        StockOrderBookEvent event = event();

        publisher.publish(event);

        assertThat(emitterRegistry.publishedEvent).isEqualTo(event);
    }

    private StockOrderBookEvent event() {
        StockOrderBook orderBook = new StockOrderBook(
                "005930",
                List.of(new StockOrderBookLevel(75100L, 100L)),
                List.of(new StockOrderBookLevel(75000L, 120L)),
                LocalDateTime.of(2026, 5, 27, 9, 0)
        );
        return StockOrderBookEvent.fromKis(orderBook, LocalDateTime.of(2026, 5, 27, 9, 0, 1));
    }

    private static class FakeStockOrderBookSseEmitterRegistry extends StockOrderBookSseEmitterRegistry {

        private StockOrderBookEvent publishedEvent;

        @Override
        public void publish(StockOrderBookEvent event) {
            this.publishedEvent = event;
        }
    }
}
