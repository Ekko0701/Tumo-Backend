package com.tumo.stock.dto;

import com.tumo.stock.domain.candle.CandleInterval;
import com.tumo.stock.domain.candle.StockCandle;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "종목 캔들(차트) 목록 응답")
public record StockCandleListResponse(
        @Schema(description = "종목 코드", example = "005930")
        String stockCode,

        @Schema(description = "캔들 시간 단위", example = "DAY")
        String interval,

        @Schema(description = "캔들 기준 시각 오름차순 캔들 목록")
        List<StockCandleResponse> candles
) {

    public static StockCandleListResponse of(String stockCode, CandleInterval interval, List<StockCandle> candles) {
        return new StockCandleListResponse(
                stockCode,
                interval.name(),
                candles.stream()
                        .map(StockCandleResponse::from)
                        .toList()
        );
    }
}
