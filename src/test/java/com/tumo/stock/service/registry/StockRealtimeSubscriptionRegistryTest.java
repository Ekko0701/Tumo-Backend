package com.tumo.stock.service.registry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class StockRealtimeSubscriptionRegistryTest {

    private final StockRealtimeSubscriptionRegistry registry = new StockRealtimeSubscriptionRegistry();

    @Test
    void acquirePriceSubscriptionsReturnsOnlyNewlySubscribedStockCodes() {
        List<String> firstResult = registry.acquirePriceSubscriptions(List.of("005930", "000660"));
        List<String> secondResult = registry.acquirePriceSubscriptions(List.of("005930", "000660", "035420"));

        assertThat(firstResult).containsExactly("005930", "000660");
        // 이미 잡힌 종목은 참조만 늘고, 새로 0→1이 된 종목만 반환한다.
        assertThat(secondResult).containsExactly("035420");
    }

    @Test
    void releasePriceSubscriptionsOnlyUnsubscribesWhenLastReferenceIsGone() {
        registry.acquirePriceSubscriptions(List.of("005930"));
        registry.acquirePriceSubscriptions(List.of("005930"));

        // 참조가 2 → 1로 줄 뿐이라 아직 해제되지 않는다.
        assertThat(registry.releasePriceSubscriptions(List.of("005930"))).isEmpty();
        assertThat(registry.getSubscribedPriceStockCodes()).containsExactly("005930");

        // 마지막 참조가 사라지면(1→0) 해제 대상으로 반환된다.
        assertThat(registry.releasePriceSubscriptions(List.of("005930"))).containsExactly("005930");
        assertThat(registry.getSubscribedPriceStockCodes()).isEmpty();
    }

    @Test
    void acquireOrderBookSubscriptionsReturnsOnlyNewStockCodes() {
        List<String> firstResult = registry.acquireOrderBookSubscriptions(List.of("005930", "000660"));
        List<String> secondResult = registry.acquireOrderBookSubscriptions(List.of("005930", "000660", "035420"));

        assertThat(firstResult).containsExactly("005930", "000660");
        assertThat(secondResult).containsExactly("035420");
    }

    @Test
    void priceAndOrderBookSubscriptionsAreManagedSeparately() {
        registry.acquirePriceSubscriptions(List.of("005930"));

        List<String> result = registry.acquireOrderBookSubscriptions(List.of("005930"));

        assertThat(result).containsExactly("005930");
    }

    @Test
    void getSubscribedPriceStockCodesReturnsSortedStockCodes() {
        registry.acquirePriceSubscriptions(List.of("005930", "000660"));

        List<String> result = registry.getSubscribedPriceStockCodes();

        assertThat(result).containsExactly("000660", "005930");
    }

    @Test
    void getSubscribedOrderBookStockCodesReturnsSortedStockCodes() {
        registry.acquireOrderBookSubscriptions(List.of("005930", "000660"));

        List<String> result = registry.getSubscribedOrderBookStockCodes();

        assertThat(result).containsExactly("000660", "005930");
    }

    @Test
    void releasedPriceSubscriptionCanBeAcquiredAgainAsNew() {
        registry.acquirePriceSubscriptions(List.of("005930"));
        registry.releasePriceSubscriptions(List.of("005930"));

        List<String> result = registry.acquirePriceSubscriptions(List.of("005930"));

        assertThat(result).containsExactly("005930");
    }

    @Test
    void releaseOrderBookSubscriptionsRemovesSubscribedStockCodes() {
        registry.acquireOrderBookSubscriptions(List.of("005930"));
        registry.releaseOrderBookSubscriptions(List.of("005930"));

        List<String> result = registry.acquireOrderBookSubscriptions(List.of("005930"));

        assertThat(result).containsExactly("005930");
    }
}
