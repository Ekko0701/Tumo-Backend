package com.tumo.stock.adapter.out.kis.rest.candle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tumo.stock.adapter.out.kis.rest.candle.KisMinuteChartResponse.KisMinuteCandle;
import com.tumo.stock.domain.candle.CandleInterval;
import com.tumo.stock.domain.candle.StockCandle;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class KisMinuteChartResponseTest {

    private static final LocalDate FALLBACK_DATE = LocalDate.of(2025, 6, 16);

    @Test
    void toStockCandlesParsesDateAndTime() {
        KisMinuteChartResponse response = new KisMinuteChartResponse(
                "0",
                "정상처리 되었습니다.",
                List.of(new KisMinuteCandle(
                        "20250616", "090100", "53000", "53800", "52600", "53400", "1500", "80000000"))
        );

        List<StockCandle> candles = response.toStockCandles("005930", FALLBACK_DATE);

        assertThat(candles).hasSize(1);
        StockCandle candle = candles.get(0);
        assertThat(candle.getInterval()).isEqualTo(CandleInterval.MINUTE);
        assertThat(candle.getCandleTime()).isEqualTo("2025-06-16T09:01:00");
        assertThat(candle.getClosePrice()).isEqualTo(53400L);
        assertThat(candle.getTradeVolume()).isEqualTo(1500L);
    }

    @Test
    void toStockCandlesUsesFallbackDateWhenBusinessDateIsBlank() {
        KisMinuteChartResponse response = new KisMinuteChartResponse(
                "0",
                "정상처리 되었습니다.",
                List.of(new KisMinuteCandle(
                        "", "100000", "53000", "53800", "52600", "53400", "1500", "80000000"))
        );

        List<StockCandle> candles = response.toStockCandles("005930", FALLBACK_DATE);

        assertThat(candles).hasSize(1);
        assertThat(candles.get(0).getCandleTime()).isEqualTo("2025-06-16T10:00:00");
    }

    @Test
    void toStockCandlesSkipsRowsWithoutTradeTime() {
        KisMinuteChartResponse response = new KisMinuteChartResponse(
                "0",
                "정상처리 되었습니다.",
                List.of(new KisMinuteCandle(
                        "20250616", "", "0", "0", "0", "0", "0", "0"))
        );

        assertThat(response.toStockCandles("005930", FALLBACK_DATE)).isEmpty();
    }

    @Test
    void throwsWhenKisResponseIsFailure() {
        KisMinuteChartResponse response = new KisMinuteChartResponse("1", "오류가 발생했습니다.", null);

        assertThatThrownBy(() -> response.toStockCandles("005930", FALLBACK_DATE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("KIS 분봉 조회에 실패했습니다.");
    }
}
