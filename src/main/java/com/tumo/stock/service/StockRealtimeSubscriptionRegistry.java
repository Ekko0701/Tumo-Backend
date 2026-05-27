package com.tumo.stock.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Backend가 현재 구독 중인 실시간 시세 종목 상태를 메모리에서 관리하는 registry.
 */
@Component
public class StockRealtimeSubscriptionRegistry {

    /**
     * 실시간 체결가를 구독 중인 종목 코드 목록.
     */
    private final Set<String> subscribedPriceStockCodes = ConcurrentHashMap.newKeySet();

    /**
     * 실시간 호가를 구독 중인 종목 코드 목록.
     */
    private final Set<String> subscribedOrderBookStockCodes = ConcurrentHashMap.newKeySet();

    /**
     * 실시간 체결가 신규 구독 종목만 등록하고 반환한다.
     *
     * @param stockCodes 구독을 시도할 종목 코드 목록
     * @return 아직 구독 중이 아니어서 새로 등록된 종목 코드 목록
     */
    public List<String> registerNewPriceSubscriptions(Collection<String> stockCodes) {
        return registerNewSubscriptions(stockCodes, subscribedPriceStockCodes);
    }

    /**
     * 실시간 호가 신규 구독 종목만 등록하고 반환한다.
     *
     * @param stockCodes 구독을 시도할 종목 코드 목록
     * @return 아직 구독 중이 아니어서 새로 등록된 종목 코드 목록
     */
    public List<String> registerNewOrderBookSubscriptions(Collection<String> stockCodes) {
        return registerNewSubscriptions(stockCodes, subscribedOrderBookStockCodes);
    }

    /**
     * 실시간 체결가를 구독 중인 종목 코드 목록을 조회한다.
     *
     * @return 실시간 체결가를 구독 중인 종목 코드 목록
     */
    public List<String> getSubscribedPriceStockCodes() {
        return subscribedPriceStockCodes.stream()
                .sorted()
                .toList();
    }

    /**
     * 실시간 호가를 구독 중인 종목 코드 목록을 조회한다.
     *
     * @return 실시간 호가를 구독 중인 종목 코드 목록
     */
    public List<String> getSubscribedOrderBookStockCodes() {
        return subscribedOrderBookStockCodes.stream()
                .sorted()
                .toList();
    }

    /**
     * 실시간 체결가 구독 상태에서 종목 코드를 제거한다.
     *
     * @param stockCodes 구독 해제한 종목 코드 목록
     */
    public void unregisterPriceSubscriptions(Collection<String> stockCodes) {
        subscribedPriceStockCodes.removeAll(stockCodes);
    }

    /**
     * 실시간 호가 구독 상태에서 종목 코드를 제거한다.
     *
     * @param stockCodes 구독 해제한 종목 코드 목록
     */
    public void unregisterOrderBookSubscriptions(Collection<String> stockCodes) {
        subscribedOrderBookStockCodes.removeAll(stockCodes);
    }

    private List<String> registerNewSubscriptions(
            Collection<String> stockCodes,
            Set<String> subscribedStockCodes
    ) {
        return stockCodes.stream()
                .filter(subscribedStockCodes::add)
                .toList();
    }
}
