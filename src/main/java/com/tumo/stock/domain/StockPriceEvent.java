package com.tumo.stock.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 외부 시세 provider 또는 내부 시스템에서 수신한 종목 가격 변경 이벤트.
 *
 * @param price 이벤트에 포함된 종목 가격 상태
 * @param provider 가격 이벤트를 전달한 provider 이름
 * @param receivedAt Backend가 가격 이벤트를 수신한 시각
 */
public record StockPriceEvent(
        StockPrice price,
        String provider,
        LocalDateTime receivedAt
) {

    public StockPriceEvent {
        Objects.requireNonNull(price, "가격 정보는 필수입니다.");
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("가격 provider는 필수입니다.");
        }
        Objects.requireNonNull(receivedAt, "가격 이벤트 수신 시각은 필수입니다.");
    }

    public static StockPriceEvent fromKis(StockPrice price, LocalDateTime receivedAt) {
        return new StockPriceEvent(price, "KIS", receivedAt);
    }
}
