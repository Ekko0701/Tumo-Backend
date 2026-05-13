package com.tumo.order.dto;

import com.tumo.order.domain.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "주문 응답")
public record OrderResponse(
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

        @Schema(description = "주문 후 현금 잔고", example = "9250000")
        Long cashBalance,

        @Schema(description = "체결 시각", example = "2026-05-13T17:20:00")
        LocalDateTime executedAt
) {

    public static OrderResponse from(Order order, Long cashBalance) {
        return new OrderResponse(
                order.getId(),
                order.getStock().getStockCode(),
                order.getStock().getStockName(),
                order.getOrderType().name(),
                order.getQuantity(),
                order.getExecutedPrice(),
                order.getTotalAmount(),
                cashBalance,
                order.getExecutedAt()
        );
    }
}
