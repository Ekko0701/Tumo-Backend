package com.tumo.stock.dto;

import com.tumo.stock.domain.price.StockPrice;
import com.tumo.stock.domain.ranking.StockRanking;
import com.tumo.stock.domain.stock.Stock;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
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

        @Schema(description = "전일 대비 가격 변화량", example = "100")
        Long changePrice,

        @Schema(description = "전일 대비 가격 변화율", example = "0.13")
        BigDecimal changeRate,

        @Schema(description = "누적 거래량", example = "1234567")
        Long tradeVolume,

        @Schema(description = "누적 거래대금", example = "92592592500")
        Long tradeAmount,

        @Schema(description = "현재가 기준 시각", example = "2026-05-13T15:30:00")
        LocalDateTime priceChangedAt
) {

    public StockResponse(
            String stockCode,
            String stockName,
            String market,
            Long currentPrice,
            LocalDateTime priceChangedAt
    ) {
        this(
                stockCode,
                stockName,
                market,
                currentPrice,
                null,
                null,
                null,
                null,
                priceChangedAt
        );
    }

    public static StockResponse from(Stock stock) {
        return new StockResponse(
                stock.getStockCode(),
                stock.getStockName(),
                stock.getMarket().name(),
                stock.getCurrentPrice(),
                null,
                null,
                null,
                null,
                stock.getPriceChangedAt()
        );
    }

    public static StockResponse from(Stock stock, StockPrice stockPrice) {
        if (stockPrice == null) {
            return from(stock);
        }

        return new StockResponse(
                stock.getStockCode(),
                stock.getStockName(),
                stock.getMarket().name(),
                stockPrice.currentPrice(),
                stockPrice.changePrice(),
                stockPrice.changeRate(),
                stockPrice.tradeVolume(),
                stockPrice.tradeAmount(),
                stockPrice.priceChangedAt()
        );
    }

    public static StockResponse from(StockRanking stockRanking) {
        return new StockResponse(
                stockRanking.stockCode(),
                stockRanking.stockName(),
                stockRanking.market().name(),
                stockRanking.currentPrice(),
                stockRanking.changePrice(),
                stockRanking.changeRate(),
                stockRanking.tradeVolume(),
                stockRanking.tradeAmount(),
                stockRanking.rankedAt()
        );
    }
}
