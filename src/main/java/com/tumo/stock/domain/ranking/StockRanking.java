package com.tumo.stock.domain.ranking;

import com.tumo.stock.domain.stock.Market;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 특정 랭킹 기준에 포함된 종목 시세 정보.
 *
 * @param stockCode 종목 코드
 * @param stockName 종목명
 * @param market 시장 구분
 * @param currentPrice 현재가
 * @param changePrice 전일 대비 가격 변화량
 * @param changeRate 전일 대비 가격 변화율
 * @param tradeVolume 누적 거래량
 * @param tradeAmount 누적 거래대금
 * @param rankedAt 랭킹 조회 시각
 */
public record StockRanking(
        String stockCode,
        String stockName,
        Market market,
        Long currentPrice,
        Long changePrice,
        BigDecimal changeRate,
        Long tradeVolume,
        Long tradeAmount,
        LocalDateTime rankedAt
) {

    public StockRanking {
        if (stockCode == null || stockCode.isBlank()) {
            throw new IllegalArgumentException("종목 코드는 필수입니다.");
        }
        if (stockName == null || stockName.isBlank()) {
            throw new IllegalArgumentException("종목명은 필수입니다.");
        }
        Objects.requireNonNull(market, "시장 구분은 필수입니다.");
        Objects.requireNonNull(currentPrice, "현재가는 필수입니다.");
        Objects.requireNonNull(rankedAt, "랭킹 조회 시각은 필수입니다.");

        if (currentPrice < 0) {
            throw new IllegalArgumentException("현재가는 0 이상이어야 합니다.");
        }
        if (tradeVolume != null && tradeVolume < 0) {
            throw new IllegalArgumentException("거래량은 0 이상이어야 합니다.");
        }
        if (tradeAmount != null && tradeAmount < 0) {
            throw new IllegalArgumentException("거래대금은 0 이상이어야 합니다.");
        }
    }
}
