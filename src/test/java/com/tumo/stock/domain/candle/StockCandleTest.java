package com.tumo.stock.domain.candle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class StockCandleTest {

    private static final LocalDateTime CANDLE_TIME = LocalDateTime.of(2025, 6, 16, 9, 1);

    @Test
    void createsCandle() {
        StockCandle candle = new StockCandle(
                "005930", CandleInterval.MINUTE, CANDLE_TIME,
                53000L, 53800L, 52600L, 53400L, 12345678L, 658000000000L
        );

        assertThat(candle.getStockCode()).isEqualTo("005930");
        assertThat(candle.getInterval()).isEqualTo(CandleInterval.MINUTE);
        assertThat(candle.getCandleTime()).isEqualTo(CANDLE_TIME);
        assertThat(candle.getOpenPrice()).isEqualTo(53000L);
        assertThat(candle.getHighPrice()).isEqualTo(53800L);
        assertThat(candle.getLowPrice()).isEqualTo(52600L);
        assertThat(candle.getClosePrice()).isEqualTo(53400L);
        assertThat(candle.getTradeVolume()).isEqualTo(12345678L);
        assertThat(candle.getTradeAmount()).isEqualTo(658000000000L);
    }

    @Test
    void throwsWhenStockCodeIsBlank() {
        assertThatThrownBy(() -> new StockCandle(
                " ", CandleInterval.DAY, CANDLE_TIME, 1L, 1L, 1L, 1L, 1L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwsWhenPriceIsNegative() {
        assertThatThrownBy(() -> new StockCandle(
                "005930", CandleInterval.DAY, CANDLE_TIME, -1L, 1L, 1L, 1L, 1L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwsWhenIntervalIsNull() {
        assertThatThrownBy(() -> new StockCandle(
                "005930", null, CANDLE_TIME, 1L, 1L, 1L, 1L, 1L, 1L))
                .isInstanceOf(NullPointerException.class);
    }
}
