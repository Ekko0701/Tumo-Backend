package com.tumo.stock.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class StockPriceEventTest {

    @Test
    void createStockPriceEvent() {
        LocalDateTime receivedAt = LocalDateTime.of(2026, 5, 25, 9, 1, 1);
        StockPrice price = createStockPrice();

        StockPriceEvent event = new StockPriceEvent(price, "KIS", receivedAt);

        assertThat(event.price()).isEqualTo(price);
        assertThat(event.provider()).isEqualTo("KIS");
        assertThat(event.receivedAt()).isEqualTo(receivedAt);
    }

    @Test
    void createKisStockPriceEvent() {
        LocalDateTime receivedAt = LocalDateTime.of(2026, 5, 25, 9, 1, 1);
        StockPrice price = createStockPrice();

        StockPriceEvent event = StockPriceEvent.fromKis(price, receivedAt);

        assertThat(event.price()).isEqualTo(price);
        assertThat(event.provider()).isEqualTo("KIS");
        assertThat(event.receivedAt()).isEqualTo(receivedAt);
    }

    @Test
    void throwsExceptionWhenPriceIsNull() {
        assertThatThrownBy(() -> new StockPriceEvent(
                null,
                "KIS",
                LocalDateTime.of(2026, 5, 25, 9, 1, 1)
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("가격 정보는 필수입니다.");
    }

    @Test
    void throwsExceptionWhenProviderIsBlank() {
        assertThatThrownBy(() -> new StockPriceEvent(
                createStockPrice(),
                " ",
                LocalDateTime.of(2026, 5, 25, 9, 1, 1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가격 provider는 필수입니다.");
    }

    @Test
    void throwsExceptionWhenReceivedAtIsNull() {
        assertThatThrownBy(() -> new StockPriceEvent(
                createStockPrice(),
                "KIS",
                null
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("가격 이벤트 수신 시각은 필수입니다.");
    }

    private StockPrice createStockPrice() {
        return new StockPrice(
                "005930",
                75100L,
                100L,
                BigDecimal.valueOf(0.13),
                1234567L,
                LocalDateTime.of(2026, 5, 25, 9, 1)
        );
    }
}
