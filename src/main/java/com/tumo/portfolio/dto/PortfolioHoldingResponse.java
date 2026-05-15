package com.tumo.portfolio.dto;

import com.tumo.holding.domain.Holding;
import com.tumo.stock.domain.Stock;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포트폴리오 보유 종목 응답")
public record PortfolioHoldingResponse(
        @Schema(description = "종목 코드", example = "005930")
        String stockCode,

        @Schema(description = "종목명", example = "삼성전자")
        String stockName,

        @Schema(description = "보유 수량", example = "10")
        Long quantity,

        @Schema(description = "평균 매입가", example = "75000")
        Long averagePrice,

        @Schema(description = "현재가", example = "75000")
        Long currentPrice,

        @Schema(description = "평가 금액", example = "750000")
        Long evaluationAmount,

        @Schema(description = "평가손익 금액", example = "0")
        Long profitAmount,

        @Schema(description = "수익률", example = "0.0")
        Double profitRate
) {

    public static PortfolioHoldingResponse from(Holding holding) {
        Stock stock = holding.getStock();
        Long evaluationAmount = stock.getCurrentPrice() * holding.getQuantity();
        Long profitAmount = (stock.getCurrentPrice() - holding.getAveragePrice()) * holding.getQuantity();
        Double profitRate = calculateProfitRate(profitAmount, holding.getAveragePrice() * holding.getQuantity());

        return new PortfolioHoldingResponse(
                stock.getStockCode(),
                stock.getStockName(),
                holding.getQuantity(),
                holding.getAveragePrice(),
                stock.getCurrentPrice(),
                evaluationAmount,
                profitAmount,
                profitRate
        );
    }

    private static Double calculateProfitRate(Long profitAmount, Long purchaseAmount) {
        if (purchaseAmount == 0) {
            return 0.0;
        }

        return profitAmount * 100.0 / purchaseAmount;
    }
}
