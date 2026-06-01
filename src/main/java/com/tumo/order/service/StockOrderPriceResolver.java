package com.tumo.order.service;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.domain.price.StockPrice;
import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.port.query.StockPriceQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 주문 체결에 사용할 종목 현재가를 확정하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class StockOrderPriceResolver {

    /**
     * 외부 시세 provider 또는 가격 저장소에서 최신 현재가를 조회하는 port.
     */
    private final StockPriceQueryPort stockPriceQueryPort;

    /**
     * 주문 직전 최신 현재가를 조회하고 종목의 마지막 가격 상태를 갱신한다.
     *
     * @param stock 주문 대상 종목
     * @return 주문 체결에 사용할 현재가
     */
    public Long resolve(Stock stock) {
        StockPrice stockPrice = stockPriceQueryPort.findCurrentPrice(stock.getStockCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_PRICE_UNAVAILABLE));

        stock.updatePrice(stockPrice.currentPrice(), stockPrice.priceChangedAt());

        return stockPrice.currentPrice();
    }
}
