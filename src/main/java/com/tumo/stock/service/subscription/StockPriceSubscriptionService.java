package com.tumo.stock.service.subscription;

import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.port.client.StockRealtimePriceClient;
import com.tumo.stock.repository.StockRepository;
import com.tumo.stock.service.realtime.StockRealtimePriceService;
import com.tumo.stock.service.registry.StockRealtimeSubscriptionRegistry;
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
    private final StockRealtimeSubscriptionRegistry stockRealtimeSubscriptionRegistry;

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

        List<String> newStockCodes = stockRealtimeSubscriptionRegistry.registerNewPriceSubscriptions(stockCodes);

        if (newStockCodes.isEmpty()) {
            return;
        }

        try {
            stockRealtimePriceClient.subscribe(newStockCodes, stockRealtimePriceService::handle);
        } catch (RuntimeException exception) {
            stockRealtimeSubscriptionRegistry.unregisterPriceSubscriptions(newStockCodes);
            throw exception;
        }
    }
}
