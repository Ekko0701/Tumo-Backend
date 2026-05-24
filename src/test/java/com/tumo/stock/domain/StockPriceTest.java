package com.tumo.stock.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class StockPriceTest {

    @Test
    void createStockPrice() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 24, 9, 1);

        StockPrice stockPrice = new StockPrice(
                "005930",
                75100L,
                100L,
                BigDecimal.valueOf(0.13),
                1234567L,
                priceChangedAt
        );

        assertThat(stockPrice.stockCode()).isEqualTo("005930");
        assertThat(stockPrice.currentPrice()).isEqualTo(75100L);
        assertThat(stockPrice.changePrice()).isEqualTo(100L);
        assertThat(stockPrice.changeRate()).isEqualByComparingTo("0.13");
        assertThat(stockPrice.tradeVolume()).isEqualTo(1234567L);
        assertThat(stockPrice.priceChangedAt()).isEqualTo(priceChangedAt);
    }

    @Test
    void throwsExceptionWhenStockCodeIsBlank() {
        assertThatThrownBy(() -> new StockPrice(
                " ",
                75100L,
                100L,
                BigDecimal.valueOf(0.13),
                1234567L,
                LocalDateTime.of(2026, 5, 24, 9, 1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종목 코드는 필수입니다.");
    }

    @Test
    void throwsExceptionWhenCurrentPriceIsNegative() {
        assertThatThrownBy(() -> new StockPrice(
                "005930",
                -1L,
                100L,
                BigDecimal.valueOf(0.13),
                1234567L,
                LocalDateTime.of(2026, 5, 24, 9, 1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재가는 0 이상이어야 합니다.");
    }

    @Test
    void throwsExceptionWhenTradeVolumeIsNegative() {
        assertThatThrownBy(() -> new StockPrice(
                "005930",
                75100L,
                100L,
                BigDecimal.valueOf(0.13),
                -1L,
                LocalDateTime.of(2026, 5, 24, 9, 1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("거래량은 0 이상이어야 합니다.");
    }
}
