package com.tumo.order.dto;

import com.tumo.order.domain.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "주문 요청")
public record OrderRequest(
        @Schema(description = "주문할 종목 코드", example = "005930")
        @NotBlank(message = "종목 코드는 필수입니다.")
        String stockCode,

        @Schema(description = "주문 수량", example = "10")
        @NotNull(message = "주문 수량은 필수입니다.")
        @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다.")
        Long quantity,

        @Schema(description = "주문 유형. Phase 1에서는 BUY만 지원", example = "BUY")
        @NotNull(message = "주문 유형은 필수입니다.")
        OrderType orderType
) {
}
