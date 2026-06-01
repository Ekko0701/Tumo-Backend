package com.tumo.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "종목 page 응답")
public record StockPageResponse(
        @Schema(description = "종목 목록")
        List<StockResponse> stocks,

        @Schema(description = "현재 page 번호", example = "0")
        Integer page,

        @Schema(description = "요청 page 크기", example = "30")
        Integer size,

        @Schema(description = "다음 page 존재 여부", example = "true")
        Boolean hasNext
) {
}
