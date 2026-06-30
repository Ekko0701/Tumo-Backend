package com.tumo.stock.dto;

import com.tumo.stock.domain.candle.StockCandle;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "단일 캔들(OHLCV) 응답")
public record StockCandleResponse(
        @Schema(description = "캔들 기준 시각", example = "2025-06-16T09:01:00")
        LocalDateTime candleTime,

        @Schema(description = "시가", example = "53000")
        Long openPrice,

        @Schema(description = "고가", example = "53800")
        Long highPrice,

        @Schema(description = "저가", example = "52600")
        Long lowPrice,

        @Schema(description = "종가", example = "53400")
        Long closePrice,

        @Schema(description = "거래량", example = "12345678")
        Long tradeVolume,

        @Schema(description = "거래대금", example = "658000000000")
        Long tradeAmount
) {

    public static StockCandleResponse from(StockCandle candle) {
        return new StockCandleResponse(
                candle.getCandleTime(),
                candle.getOpenPrice(),
                candle.getHighPrice(),
                candle.getLowPrice(),
                candle.getClosePrice(),
                candle.getTradeVolume(),
                candle.getTradeAmount()
        );
    }
}
