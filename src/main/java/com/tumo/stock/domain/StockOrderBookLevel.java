package com.tumo.stock.domain;

import java.util.Objects;

/**
 * 특정 가격대의 호가 정보를 표현하는 값 객체.
 *
 * @param price 매수 또는 매도 주문 가격
 * @param volume 해당 가격대에 대기 중인 주문 수량
 */
public record StockOrderBookLevel(
        Long price,
        Long volume
) {

    public StockOrderBookLevel {
        Objects.requireNonNull(price, "호가 가격은 필수입니다.");
        Objects.requireNonNull(volume, "호가 잔량은 필수입니다.");

        if (price < 0) {
            throw new IllegalArgumentException("호가 가격은 0 이상이어야 합니다.");
        }
        if (volume < 0) {
            throw new IllegalArgumentException("호가 잔량은 0 이상이어야 합니다.");
        }
    }
}
