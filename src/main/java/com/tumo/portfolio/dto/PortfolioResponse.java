package com.tumo.portfolio.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "내 포트폴리오 응답")
public record PortfolioResponse(
        @Schema(description = "현금 잔고", example = "9250000")
        Long cashBalance,

        @Schema(description = "보유 주식 평가 금액 합계", example = "750000")
        Long totalStockValue,

        @Schema(description = "총 자산. 현금 + 보유 주식 평가 금액", example = "10000000")
        Long totalAsset,

        @Schema(description = "전체 평가손익 금액", example = "0")
        Long profitAmount,

        @Schema(description = "전체 수익률", example = "0.0")
        Double profitRate,

        @Schema(description = "보유 종목 목록")
        List<PortfolioHoldingResponse> holdings
) {
}
