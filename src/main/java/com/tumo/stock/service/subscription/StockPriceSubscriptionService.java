package com.tumo.stock.service.subscription;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
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
     * 지정한 종목의 실시간 가격 이벤트 구독을 시작한다.
     *
     * @param stockCodes 실시간 가격 이벤트를 구독할 종목 코드 목록
     */
    public void subscribe(List<String> stockCodes) {
        if (stockCodes == null || stockCodes.isEmpty()) {
            return;
        }

        stockCodes.forEach(stockCode -> stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND)));

        subscribeNewStocks(stockCodes);
    }

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

        subscribeNewStocks(stockCodes);
    }

    private void subscribeNewStocks(List<String> stockCodes) {
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
