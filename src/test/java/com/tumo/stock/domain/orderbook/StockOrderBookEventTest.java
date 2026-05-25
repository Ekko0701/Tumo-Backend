package com.tumo.stock.domain.orderbook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class StockOrderBookEventTest {

    @Test
    void createStockOrderBookEvent() {
        LocalDateTime receivedAt = LocalDateTime.of(2026, 5, 25, 9, 1, 1);
        StockOrderBook orderBook = createOrderBook();

        StockOrderBookEvent event = new StockOrderBookEvent(orderBook, "KIS", receivedAt);

        assertThat(event.orderBook()).isEqualTo(orderBook);
        assertThat(event.provider()).isEqualTo("KIS");
        assertThat(event.receivedAt()).isEqualTo(receivedAt);
    }

    @Test
    void createKisStockOrderBookEvent() {
        LocalDateTime receivedAt = LocalDateTime.of(2026, 5, 25, 9, 1, 1);
        StockOrderBook orderBook = createOrderBook();

        StockOrderBookEvent event = StockOrderBookEvent.fromKis(orderBook, receivedAt);

        assertThat(event.orderBook()).isEqualTo(orderBook);
        assertThat(event.provider()).isEqualTo("KIS");
        assertThat(event.receivedAt()).isEqualTo(receivedAt);
    }

    @Test
    void throwsExceptionWhenOrderBookIsNull() {
        assertThatThrownBy(() -> new StockOrderBookEvent(
                null,
                "KIS",
                LocalDateTime.of(2026, 5, 25, 9, 1, 1)
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("호가 정보는 필수입니다.");
    }

    @Test
    void throwsExceptionWhenProviderIsBlank() {
        assertThatThrownBy(() -> new StockOrderBookEvent(
                createOrderBook(),
                " ",
                LocalDateTime.of(2026, 5, 25, 9, 1, 1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("호가 provider는 필수입니다.");
    }

    @Test
    void throwsExceptionWhenReceivedAtIsNull() {
        assertThatThrownBy(() -> new StockOrderBookEvent(
                createOrderBook(),
                "KIS",
                null
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("호가 이벤트 수신 시각은 필수입니다.");
    }

    private StockOrderBook createOrderBook() {
        return new StockOrderBook(
                "005930",
                List.of(new StockOrderBookLevel(75200L, 3000L)),
                List.of(new StockOrderBookLevel(75100L, 2500L)),
                LocalDateTime.of(2026, 5, 25, 9, 1)
        );
    }
}
