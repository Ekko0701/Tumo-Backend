package com.tumo.stock.adapter.out.sse.price;

import static org.assertj.core.api.Assertions.assertThat;

import com.tumo.stock.domain.price.StockPrice;
import com.tumo.stock.domain.price.StockPriceEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SseStockPricePublisherTest {

    @Test
    void publish() {
        FakeStockPriceSseEmitterRegistry emitterRegistry = new FakeStockPriceSseEmitterRegistry();
        SseStockPricePublisher publisher = new SseStockPricePublisher(emitterRegistry);
        StockPriceEvent event = event();

        publisher.publish(event);

        assertThat(emitterRegistry.publishedEvent).isEqualTo(event);
    }

    private StockPriceEvent event() {
        StockPrice price = new StockPrice(
                "005930",
                75000L,
                1000L,
                BigDecimal.valueOf(1.35),
                1000000L,
                LocalDateTime.of(2026, 5, 27, 9, 0)
        );
        return StockPriceEvent.fromKis(price, LocalDateTime.of(2026, 5, 27, 9, 0, 1));
    }

    private static class FakeStockPriceSseEmitterRegistry extends StockPriceSseEmitterRegistry {

        private StockPriceEvent publishedEvent;

        @Override
        public void publish(StockPriceEvent event) {
            this.publishedEvent = event;
        }
    }
}
