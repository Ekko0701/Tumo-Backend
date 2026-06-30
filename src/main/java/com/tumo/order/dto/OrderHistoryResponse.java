package com.tumo.order.dto;

import com.tumo.order.domain.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "주문 내역 응답")
public record OrderHistoryResponse(
        @Schema(description = "주문 ID", example = "1")
        Long orderId,

        @Schema(description = "종목 코드", example = "005930")
        String stockCode,

        @Schema(description = "종목명", example = "삼성전자")
        String stockName,

        @Schema(description = "주문 유형", example = "BUY")
        String orderType,

        @Schema(description = "체결 수량", example = "10")
        Long quantity,

        @Schema(description = "체결가", example = "75000")
        Long executedPrice,

        @Schema(description = "총 체결 금액", example = "750000")
        Long totalAmount,

        @Schema(description = "실현손익 (매도 주문만 존재, 매수는 null)", example = "50000")
        Long realizedProfit,

        @Schema(description = "체결 시각", example = "2026-05-13T17:20:00")
        LocalDateTime executedAt
) {

    public static OrderHistoryResponse from(Order order) {
        return new OrderHistoryResponse(
                order.getId(),
                order.getStock().getStockCode(),
                order.getStock().getStockName(),
                order.getOrderType().name(),
                order.getQuantity(),
                order.getExecutedPrice(),
                order.getTotalAmount(),
                order.getRealizedProfit(),
                order.getExecutedAt()
        );
    }
}
