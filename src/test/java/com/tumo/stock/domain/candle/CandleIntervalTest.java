package com.tumo.stock.domain.candle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CandleIntervalTest {

    @Test
    void mapsPeriodDivisionCodes() {
        assertThat(CandleInterval.DAY.kisPeriodCode()).isEqualTo("D");
        assertThat(CandleInterval.WEEK.kisPeriodCode()).isEqualTo("W");
        assertThat(CandleInterval.MONTH.kisPeriodCode()).isEqualTo("M");
        assertThat(CandleInterval.YEAR.kisPeriodCode()).isEqualTo("Y");
    }

    @Test
    void isMinuteReturnsTrueOnlyForMinute() {
        assertThat(CandleInterval.MINUTE.isMinute()).isTrue();
        assertThat(CandleInterval.DAY.isMinute()).isFalse();
        assertThat(CandleInterval.YEAR.isMinute()).isFalse();
    }

    @Test
    void throwsWhenMinuteHasNoPeriodCode() {
        assertThatThrownBy(CandleInterval.MINUTE::kisPeriodCode)
                .isInstanceOf(IllegalStateException.class);
    }
}
