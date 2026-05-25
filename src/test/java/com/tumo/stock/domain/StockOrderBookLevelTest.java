package com.tumo.stock.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class StockOrderBookLevelTest {

    @Test
    void createStockOrderBookLevel() {
        StockOrderBookLevel level = new StockOrderBookLevel(75200L, 3000L);

        assertThat(level.price()).isEqualTo(75200L);
        assertThat(level.volume()).isEqualTo(3000L);
    }

    @Test
    void throwsExceptionWhenPriceIsNull() {
        assertThatThrownBy(() -> new StockOrderBookLevel(null, 3000L))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("호가 가격은 필수입니다.");
    }

    @Test
    void throwsExceptionWhenVolumeIsNull() {
        assertThatThrownBy(() -> new StockOrderBookLevel(75200L, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("호가 잔량은 필수입니다.");
    }

    @Test
    void throwsExceptionWhenPriceIsNegative() {
        assertThatThrownBy(() -> new StockOrderBookLevel(-1L, 3000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("호가 가격은 0 이상이어야 합니다.");
    }

    @Test
    void throwsExceptionWhenVolumeIsNegative() {
        assertThatThrownBy(() -> new StockOrderBookLevel(75200L, -1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("호가 잔량은 0 이상이어야 합니다.");
    }
}
