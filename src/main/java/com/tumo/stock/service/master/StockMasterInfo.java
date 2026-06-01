package com.tumo.stock.service.master;

import com.tumo.stock.domain.stock.Market;
import java.util.Objects;

/**
 * 종목 마스터 파일에서 추출한 Backend 내부 종목 마스터 정보.
 *
 * @param stockCode 종목 코드
 * @param stockName 종목명
 * @param market 시장 구분
 * @param basePrice 종목 마스터 파일의 주식 기준가
 */
public record StockMasterInfo(
        String stockCode,
        String stockName,
        Market market,
        Long basePrice
) {

    public StockMasterInfo {
        if (stockCode == null || stockCode.isBlank()) {
            throw new IllegalArgumentException("종목 코드는 필수입니다.");
        }
        if (stockName == null || stockName.isBlank()) {
            throw new IllegalArgumentException("종목명은 필수입니다.");
        }
        Objects.requireNonNull(market, "시장 구분은 필수입니다.");
        Objects.requireNonNull(basePrice, "기준가는 필수입니다.");
    }
}
