package com.tumo.stock.service.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Backend가 현재 구독 중인 실시간 시세 종목 상태를 메모리에서 관리하는 registry.
 *
 * <p>실시간 체결가는 여러 SSE 연결이 같은 종목을 동시에 볼 수 있어 <b>참조 카운트</b>로 관리한다.
 * 종목을 처음 잡을 때(0→1)만 KIS 구독을 시작하고, 마지막 연결이 놓을 때(1→0)만 KIS 구독을 해제하면
 * KIS 세션의 종목 등록 수가 "실제로 보고 있는 종목"으로만 유지되어 등록 한도 초과를 막을 수 있다.</p>
 */
@Component
public class StockRealtimeSubscriptionRegistry {

    /**
     * 실시간 체결가 종목별 구독 참조 수. 0이 되면 항목을 제거한다.
     */
    private final ConcurrentHashMap<String, Integer> priceSubscriptionCounts = new ConcurrentHashMap<>();

    /**
     * 실시간 호가 종목별 구독 참조 수. 0이 되면 항목을 제거한다.
     */
    private final ConcurrentHashMap<String, Integer> orderBookSubscriptionCounts = new ConcurrentHashMap<>();

    /**
     * 실시간 체결가 구독 참조를 늘리고, 이번에 처음 구독된(0→1) 종목 코드만 반환한다.
     *
     * @param stockCodes 구독 참조를 늘릴 종목 코드 목록
     * @return 이번 호출로 새로 구독을 시작해야 하는(참조가 0→1이 된) 종목 코드 목록
     */
    public List<String> acquirePriceSubscriptions(Collection<String> stockCodes) {
        return acquire(priceSubscriptionCounts, stockCodes);
    }

    /**
     * 실시간 체결가 구독 참조를 줄이고, 이번에 마지막 참조가 사라진(1→0) 종목 코드만 반환한다.
     *
     * @param stockCodes 구독 참조를 줄일 종목 코드 목록
     * @return 이번 호출로 구독을 해제해야 하는(참조가 1→0이 된) 종목 코드 목록
     */
    public List<String> releasePriceSubscriptions(Collection<String> stockCodes) {
        return release(priceSubscriptionCounts, stockCodes);
    }

    /**
     * 실시간 호가 구독 참조를 늘리고, 이번에 처음 구독된(0→1) 종목 코드만 반환한다.
     *
     * @param stockCodes 구독 참조를 늘릴 종목 코드 목록
     * @return 이번 호출로 새로 구독을 시작해야 하는(참조가 0→1이 된) 종목 코드 목록
     */
    public List<String> acquireOrderBookSubscriptions(Collection<String> stockCodes) {
        return acquire(orderBookSubscriptionCounts, stockCodes);
    }

    /**
     * 실시간 호가 구독 참조를 줄이고, 이번에 마지막 참조가 사라진(1→0) 종목 코드만 반환한다.
     *
     * @param stockCodes 구독 참조를 줄일 종목 코드 목록
     * @return 이번 호출로 구독을 해제해야 하는(참조가 1→0이 된) 종목 코드 목록
     */
    public List<String> releaseOrderBookSubscriptions(Collection<String> stockCodes) {
        return release(orderBookSubscriptionCounts, stockCodes);
    }

    /**
     * 실시간 체결가를 구독 중인 종목 코드 목록을 조회한다.
     *
     * @return 실시간 체결가를 구독 중인 종목 코드 목록
     */
    public List<String> getSubscribedPriceStockCodes() {
        return sortedKeys(priceSubscriptionCounts);
    }

    /**
     * 실시간 호가를 구독 중인 종목 코드 목록을 조회한다.
     *
     * @return 실시간 호가를 구독 중인 종목 코드 목록
     */
    public List<String> getSubscribedOrderBookStockCodes() {
        return sortedKeys(orderBookSubscriptionCounts);
    }

    /**
     * 참조 카운트를 늘리고, 이번에 0→1이 된(새로 구독해야 하는) 종목 코드만 반환한다.
     */
    private List<String> acquire(ConcurrentHashMap<String, Integer> counts, Collection<String> stockCodes) {
        List<String> newlySubscribed = new ArrayList<>();

        for (String stockCode : stockCodes) {
            int updatedCount = counts.merge(stockCode, 1, Integer::sum);
            if (updatedCount == 1) {
                newlySubscribed.add(stockCode);
            }
        }

        return List.copyOf(newlySubscribed);
    }

    /**
     * 참조 카운트를 줄이고, 이번에 1→0이 된(해제해야 하는) 종목 코드만 반환한다.
     */
    private List<String> release(ConcurrentHashMap<String, Integer> counts, Collection<String> stockCodes) {
        List<String> released = new ArrayList<>();

        for (String stockCode : stockCodes) {
            boolean[] removed = {false};
            counts.computeIfPresent(stockCode, (code, count) -> {
                if (count <= 1) {
                    removed[0] = true;
                    return null;
                }
                return count - 1;
            });

            if (removed[0]) {
                released.add(stockCode);
            }
        }

        return List.copyOf(released);
    }

    private List<String> sortedKeys(ConcurrentHashMap<String, Integer> counts) {
        return counts.keySet().stream()
                .sorted()
                .toList();
    }
}
