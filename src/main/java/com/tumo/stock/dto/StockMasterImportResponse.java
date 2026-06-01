package com.tumo.stock.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "종목 마스터 import 결과 응답")
public record StockMasterImportResponse(
        @Schema(description = "신규 저장한 종목 수", example = "1200")
        Integer importedCount,

        @Schema(description = "기존 정보를 갱신한 종목 수", example = "500")
        Integer updatedCount,

        @Schema(description = "일반 주식이 아니거나 형식이 올바르지 않아 제외한 row 수", example = "300")
        Integer skippedCount
) {
}
