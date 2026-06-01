package com.tumo.stock.service.master;

import java.util.List;

/**
 * 종목 마스터 파일 파싱 결과.
 *
 * @param stockMasters 파싱에 성공한 일반 주식 종목 마스터 목록
 * @param skippedCount 일반 주식이 아니거나 형식이 올바르지 않아 제외한 row 수
 */
public record StockMasterParseResult(
        List<StockMasterInfo> stockMasters,
        Integer skippedCount
) {
}
