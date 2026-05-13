package com.tumo.stock.service;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.domain.Stock;
import com.tumo.stock.dto.StockListResponse;
import com.tumo.stock.dto.StockResponse;
import com.tumo.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;

    public StockListResponse getStocks() {
        return new StockListResponse(
                stockRepository.findAll().stream()
                        .map(StockResponse::from)
                        .toList()
        );
    }

    public StockResponse getStock(String stockCode) {
        Stock stock = stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));

        return StockResponse.from(stock);
    }
}
