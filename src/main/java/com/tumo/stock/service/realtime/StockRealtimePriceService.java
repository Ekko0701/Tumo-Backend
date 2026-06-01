package com.tumo.stock.service.realtime;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.domain.price.StockPrice;
import com.tumo.stock.domain.price.StockPriceEvent;
import com.tumo.stock.port.publisher.StockPricePublisher;
import com.tumo.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 실시간 가격 이벤트를 처리해 종목의 마지막 가격 캐시를 갱신하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StockRealtimePriceService {

    private final StockRepository stockRepository;
    private final StockPricePublisher stockPricePublisher;

    /**
     * 수신한 가격 이벤트를 처리한다.
     *
     * @param event 처리할 가격 이벤트
     */
    public void handle(StockPriceEvent event) {
        StockPrice price = event.price();

        Stock stock = stockRepository.findByStockCode(price.stockCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));

        stock.updatePrice(price.currentPrice(), price.priceChangedAt());

        stockPricePublisher.publish(event);
    }
}
