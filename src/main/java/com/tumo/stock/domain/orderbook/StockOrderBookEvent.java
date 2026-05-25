package com.tumo.stock.domain.orderbook;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 외부 시세 provider 또는 내부 시스템에서 수신한 종목 호가 변경 이벤트.
 *
 * @param orderBook 이벤트에 포함된 종목 호가 상태
 * @param provider 호가 이벤트를 전달한 provider 이름
 * @param receivedAt Backend가 호가 이벤트를 수신한 시각
 */
public record StockOrderBookEvent(
        StockOrderBook orderBook,
        String provider,
        LocalDateTime receivedAt
) {

    public StockOrderBookEvent {
        Objects.requireNonNull(orderBook, "호가 정보는 필수입니다.");
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("호가 provider는 필수입니다.");
        }
        Objects.requireNonNull(receivedAt, "호가 이벤트 수신 시각은 필수입니다.");
    }

    public static StockOrderBookEvent fromKis(StockOrderBook orderBook, LocalDateTime receivedAt) {
        return new StockOrderBookEvent(orderBook, "KIS", receivedAt);
    }
}
