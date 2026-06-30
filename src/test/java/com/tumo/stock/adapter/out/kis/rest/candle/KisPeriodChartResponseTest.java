package com.tumo.stock.adapter.out.kis.rest.candle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tumo.stock.adapter.out.kis.rest.candle.KisPeriodChartResponse.KisPeriodCandle;
import com.tumo.stock.domain.candle.CandleInterval;
import com.tumo.stock.domain.candle.StockCandle;
import java.util.List;
import org.junit.jupiter.api.Test;

class KisPeriodChartResponseTest {

    @Test
    void toStockCandlesSortsAscendingAndMapsFields() {
        KisPeriodChartResponse response = new KisPeriodChartResponse(
                "0",
                "정상처리 되었습니다.",
                List.of(
                        new KisPeriodCandle("20250103", "54000", "54500", "53500", "54200", "200", "1000"),
                        new KisPeriodCandle("20250102", "53000", "53800", "52600", "53400", "100", "500")
                )
        );

        List<StockCandle> candles = response.toStockCandles("005930", CandleInterval.DAY);

        assertThat(candles).hasSize(2);
        StockCandle first = candles.get(0);
        assertThat(first.getCandleTime()).isEqualTo("2025-01-02T00:00:00");
        assertThat(first.getStockCode()).isEqualTo("005930");
        assertThat(first.getInterval()).isEqualTo(CandleInterval.DAY);
        assertThat(first.getOpenPrice()).isEqualTo(53000L);
        assertThat(first.getHighPrice()).isEqualTo(53800L);
        assertThat(first.getLowPrice()).isEqualTo(52600L);
        assertThat(first.getClosePrice()).isEqualTo(53400L);
        assertThat(first.getTradeVolume()).isEqualTo(100L);
        assertThat(first.getTradeAmount()).isEqualTo(500L);
        assertThat(candles.get(1).getCandleTime()).isEqualTo("2025-01-03T00:00:00");
    }

    @Test
    void toStockCandlesSkipsRowsWithoutBusinessDate() {
        KisPeriodChartResponse response = new KisPeriodChartResponse(
                "0",
                "정상처리 되었습니다.",
                List.of(
                        new KisPeriodCandle("20250102", "53000", "53800", "52600", "53400", "100", "500"),
                        new KisPeriodCandle("", "0", "0", "0", "0", "0", "0")
                )
        );

        List<StockCandle> candles = response.toStockCandles("005930", CandleInterval.DAY);

        assertThat(candles).hasSize(1);
    }

    @Test
    void toStockCandlesReturnsEmptyWhenOutputIsNull() {
        KisPeriodChartResponse response = new KisPeriodChartResponse("0", "정상처리 되었습니다.", null);

        assertThat(response.toStockCandles("005930", CandleInterval.DAY)).isEmpty();
    }

    @Test
    void throwsWhenKisResponseIsFailure() {
        KisPeriodChartResponse response = new KisPeriodChartResponse("1", "오류가 발생했습니다.", null);

        assertThatThrownBy(() -> response.toStockCandles("005930", CandleInterval.DAY))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("KIS 기간별시세 조회에 실패했습니다.");
    }
}
