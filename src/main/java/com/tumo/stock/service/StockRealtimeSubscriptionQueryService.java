package com.tumo.stock.service;

import com.tumo.stock.dto.StockRealtimeSubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 실시간 시세 구독 상태를 조회하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockRealtimeSubscriptionQueryService {

    private final StockRealtimeSubscriptionRegistry stockRealtimeSubscriptionRegistry;

    /**
     * 현재 Backend가 구독 중인 실시간 체결가/호가 종목 목록을 조회한다.
     *
     * @return 실시간 시세 구독 상태 응답
     */
    public StockRealtimeSubscriptionResponse getSubscriptions() {
        return new StockRealtimeSubscriptionResponse(
                stockRealtimeSubscriptionRegistry.getSubscribedPriceStockCodes(),
                stockRealtimeSubscriptionRegistry.getSubscribedOrderBookStockCodes()
        );
    }
}
