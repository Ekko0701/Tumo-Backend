package com.tumo.stock.service.query;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.domain.price.StockPrice;
import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.dto.StockListResponse;
import com.tumo.stock.dto.StockResponse;
import com.tumo.stock.port.query.StockPriceQueryPort;
import com.tumo.stock.repository.StockRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;
    private final StockPriceQueryPort stockPriceQueryPort;

    @Transactional
    public StockListResponse getStocks() {
        List<Stock> stocks = stockRepository.findAll();

        stocks.forEach(this::refreshCurrentPrice);

        return new StockListResponse(stocks.stream()
                .map(StockResponse::from)
                .toList());
    }

    @Transactional
    public StockResponse getStock(String stockCode) {
        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));

        refreshCurrentPrice(stock);

        return StockResponse.from(stock);
    }

    private void refreshCurrentPrice(Stock stock) {
        try {
            stockPriceQueryPort.findCurrentPrice(stock.getStockCode())
                    .ifPresent(stockPrice -> updateStockPrice(stock, stockPrice));
        } catch (RuntimeException exception) {
            log.warn("종목 현재가 조회에 실패했습니다. stockCode={}", stock.getStockCode(), exception);
        }
    }

    private void updateStockPrice(Stock stock, StockPrice stockPrice) {
        stock.updatePrice(stockPrice.currentPrice(), stockPrice.priceChangedAt());
    }
}
