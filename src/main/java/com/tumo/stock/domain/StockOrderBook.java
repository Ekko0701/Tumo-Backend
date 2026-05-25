package com.tumo.stock.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 특정 시점의 종목 호가 상태를 표현하는 값 객체.
 *
 * @param stockCode 호가가 적용되는 종목 코드
 * @param askLevels 매도 호가 목록
 * @param bidLevels 매수 호가 목록
 * @param orderBookChangedAt 호가 변경 시각
 */
public record StockOrderBook(
        String stockCode,
        List<StockOrderBookLevel> askLevels,
        List<StockOrderBookLevel> bidLevels,
        LocalDateTime orderBookChangedAt
) {

    public StockOrderBook {
        if (stockCode == null || stockCode.isBlank()) {
            throw new IllegalArgumentException("종목 코드는 필수입니다.");
        }
        Objects.requireNonNull(askLevels, "매도 호가 목록은 필수입니다.");
        Objects.requireNonNull(bidLevels, "매수 호가 목록은 필수입니다.");
        Objects.requireNonNull(orderBookChangedAt, "호가 변경 시각은 필수입니다.");

        askLevels = List.copyOf(askLevels);
        bidLevels = List.copyOf(bidLevels);
    }

    /**
     * 최우선 매도 호가를 반환한다.
     *
     * @return 매도 호가가 있으면 첫 번째 매도 호가, 없으면 null
     */
    public StockOrderBookLevel bestAsk() {
        if (askLevels.isEmpty()) {
            return null;
        }

        return askLevels.getFirst();
    }

    /**
     * 최우선 매수 호가를 반환한다.
     *
     * @return 매수 호가가 있으면 첫 번째 매수 호가, 없으면 null
     */
    public StockOrderBookLevel bestBid() {
        if (bidLevels.isEmpty()) {
            return null;
        }

        return bidLevels.getFirst();
    }

    /**
     * 최우선 매도 호가와 최우선 매수 호가의 차이를 계산한다.
     *
     * @return 최우선 매도/매수 호가가 모두 있으면 스프레드, 둘 중 하나라도 없으면 null
     */
    public Long spread() {
        StockOrderBookLevel bestAsk = bestAsk();
        StockOrderBookLevel bestBid = bestBid();

        if (bestAsk == null || bestBid == null) {
            return null;
        }

        return bestAsk.price() - bestBid.price();
    }
}
