package com.tumo.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "주문 내역 page 응답")
public record OrderPageResponse(
        @Schema(description = "주문 내역 목록")
        List<OrderHistoryResponse> orders,

        @Schema(description = "현재 page 번호", example = "0")
        Integer page,

        @Schema(description = "요청 page 크기", example = "30")
        Integer size,

        @Schema(description = "다음 page 존재 여부", example = "true")
        Boolean hasNext
) {
}
