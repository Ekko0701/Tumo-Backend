package com.tumo.stock.dto;

import com.tumo.stock.domain.Stock;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "종목 응답")
public record StockResponse(
        @Schema(description = "종목 코드", example = "005930")
        String stockCode,

        @Schema(description = "종목명", example = "삼성전자")
        String stockName,

        @Schema(description = "시장 구분", example = "KOSPI")
        String market,

        @Schema(description = "현재가", example = "75000")
        Long currentPrice,

        @Schema(description = "현재가 기준 시각", example = "2026-05-13T15:30:00")
        LocalDateTime priceChangedAt
) {

    public static StockResponse from(Stock stock) {
        return new StockResponse(
                stock.getStockCode(),
                stock.getStockName(),
                stock.getMarket().name(),
                stock.getCurrentPrice(),
                stock.getPriceChangedAt()
        );
    }
}
