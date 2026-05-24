package com.tumo.stock.port;

import com.tumo.stock.domain.StockPrice;
import java.util.Optional;

/**
 * 종목의 최신 가격을 조회하는 outbound port.
 */
public interface StockPriceQueryPort {

    /**
     * 종목 코드에 해당하는 최신 가격을 조회한다.
     *
     * @param stockCode 최신 가격을 조회할 종목 코드
     * @return 최신 가격이 있으면 `StockPrice`, 없으면 empty
     */
    Optional<StockPrice> findCurrentPrice(String stockCode);
}
