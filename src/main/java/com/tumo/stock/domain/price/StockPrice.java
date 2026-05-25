package com.tumo.stock.domain.price;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 특정 시점의 종목 가격 상태를 표현하는 값 객체.
 *
 * @param stockCode 가격이 적용되는 종목 코드
 * @param currentPrice 현재가
 * @param changePrice 전일 대비 가격 변화량
 * @param changeRate 전일 대비 가격 변화율
 * @param tradeVolume 누적 거래량
 * @param priceChangedAt 가격 변경 시각
 */
public record StockPrice(
        String stockCode,
        Long currentPrice,
        Long changePrice,
        BigDecimal changeRate,
        Long tradeVolume,
        LocalDateTime priceChangedAt
) {

    public StockPrice {
        if (stockCode == null || stockCode.isBlank()) {
            throw new IllegalArgumentException("종목 코드는 필수입니다.");
        }
        Objects.requireNonNull(currentPrice, "현재가는 필수입니다.");
        Objects.requireNonNull(priceChangedAt, "가격 변경 시각은 필수입니다.");

        if (currentPrice < 0) {
            throw new IllegalArgumentException("현재가는 0 이상이어야 합니다.");
        }
        if (tradeVolume != null && tradeVolume < 0) {
            throw new IllegalArgumentException("거래량은 0 이상이어야 합니다.");
        }
    }
}
