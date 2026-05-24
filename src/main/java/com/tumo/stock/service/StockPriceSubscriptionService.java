package com.tumo.stock.service;

import com.tumo.stock.domain.Stock;
import com.tumo.stock.port.StockRealtimePriceClient;
import com.tumo.stock.repository.StockRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Backend에 등록된 종목의 실시간 가격 구독을 시작하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockPriceSubscriptionService {

    private final StockRepository stockRepository;
    private final StockRealtimePriceClient stockRealtimePriceClient;
    private final StockRealtimePriceService stockRealtimePriceService;

    /**
     * Backend에 등록된 모든 종목의 실시간 가격 이벤트 구독을 시작한다.
     */
    public void subscribeAllStocks() {
        List<String> stockCodes = stockRepository.findAll().stream()
                .map(Stock::getStockCode)
                .toList();

        if (stockCodes.isEmpty()) {
            return;
        }

        stockRealtimePriceClient.subscribe(stockCodes, stockRealtimePriceService::handle);
    }
}
