package com.tumo.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "실시간 시세 구독 상태 응답")
public record StockRealtimeSubscriptionResponse(
        @Schema(description = "실시간 체결가 구독 중인 종목 코드 목록", example = "[\"005930\", \"000660\"]")
        List<String> priceStockCodes,

        @Schema(description = "실시간 호가 구독 중인 종목 코드 목록", example = "[\"005930\"]")
        List<String> orderBookStockCodes
) {
}
