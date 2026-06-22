package com.tumo.stock.service.subscription;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.port.client.StockRealtimeOrderBookClient;
import com.tumo.stock.repository.StockRepository;
import com.tumo.stock.service.realtime.StockOrderBookService;
import com.tumo.stock.service.registry.StockRealtimeSubscriptionRegistry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 단일 종목의 실시간 호가 구독을 시작하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockOrderBookSubscriptionService {

    private final StockRepository stockRepository;
    private final StockRealtimeOrderBookClient stockRealtimeOrderBookClient;
    private final StockOrderBookService stockOrderBookService;
    private final StockRealtimeSubscriptionRegistry stockRealtimeSubscriptionRegistry;

    /**
     * 단일 종목의 실시간 호가 이벤트 구독을 시작한다.
     *
     * @param stockCode 실시간 호가 이벤트를 구독할 종목 코드
     */
    public void subscribe(String stockCode) {
        stockRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));

        List<String> newStockCodes = stockRealtimeSubscriptionRegistry.acquireOrderBookSubscriptions(List.of(stockCode));

        if (newStockCodes.isEmpty()) {
            return;
        }

        try {
            stockRealtimeOrderBookClient.subscribe(newStockCodes, stockOrderBookService::handle);
        } catch (RuntimeException exception) {
            stockRealtimeSubscriptionRegistry.releaseOrderBookSubscriptions(newStockCodes);
            throw exception;
        }
    }

    /**
     * 단일 종목의 실시간 호가 이벤트 구독 참조를 해제한다.
     *
     * <p>SSE 연결이 끊길 때 호출되며, 마지막 참조가 사라진 종목만 실제로 KIS 구독을 해제한다.</p>
     *
     * @param stockCode 구독 참조를 해제할 종목 코드
     */
    public void unsubscribe(String stockCode) {
        if (stockCode == null || stockCode.isBlank()) {
            return;
        }

        List<String> releasedStockCodes = stockRealtimeSubscriptionRegistry.releaseOrderBookSubscriptions(List.of(stockCode));

        if (releasedStockCodes.isEmpty()) {
            return;
        }

        stockRealtimeOrderBookClient.unsubscribeOrderBook(releasedStockCodes);
    }
}
