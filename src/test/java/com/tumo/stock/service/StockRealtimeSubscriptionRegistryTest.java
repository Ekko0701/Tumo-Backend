package com.tumo.stock.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class StockRealtimeSubscriptionRegistryTest {

    private final StockRealtimeSubscriptionRegistry registry = new StockRealtimeSubscriptionRegistry();

    @Test
    void registerNewPriceSubscriptionsReturnsOnlyNewStockCodes() {
        List<String> firstResult = registry.registerNewPriceSubscriptions(List.of("005930", "000660"));
        List<String> secondResult = registry.registerNewPriceSubscriptions(List.of("005930", "000660", "035420"));

        assertThat(firstResult).containsExactly("005930", "000660");
        assertThat(secondResult).containsExactly("035420");
    }

    @Test
    void registerNewOrderBookSubscriptionsReturnsOnlyNewStockCodes() {
        List<String> firstResult = registry.registerNewOrderBookSubscriptions(List.of("005930", "000660"));
        List<String> secondResult = registry.registerNewOrderBookSubscriptions(List.of("005930", "000660", "035420"));

        assertThat(firstResult).containsExactly("005930", "000660");
        assertThat(secondResult).containsExactly("035420");
    }

    @Test
    void priceAndOrderBookSubscriptionsAreManagedSeparately() {
        registry.registerNewPriceSubscriptions(List.of("005930"));

        List<String> result = registry.registerNewOrderBookSubscriptions(List.of("005930"));

        assertThat(result).containsExactly("005930");
    }

    @Test
    void getSubscribedPriceStockCodesReturnsSortedStockCodes() {
        registry.registerNewPriceSubscriptions(List.of("005930", "000660"));

        List<String> result = registry.getSubscribedPriceStockCodes();

        assertThat(result).containsExactly("000660", "005930");
    }

    @Test
    void getSubscribedOrderBookStockCodesReturnsSortedStockCodes() {
        registry.registerNewOrderBookSubscriptions(List.of("005930", "000660"));

        List<String> result = registry.getSubscribedOrderBookStockCodes();

        assertThat(result).containsExactly("000660", "005930");
    }

    @Test
    void unregisterPriceSubscriptionsRemovesSubscribedStockCodes() {
        registry.registerNewPriceSubscriptions(List.of("005930"));
        registry.unregisterPriceSubscriptions(List.of("005930"));

        List<String> result = registry.registerNewPriceSubscriptions(List.of("005930"));

        assertThat(result).containsExactly("005930");
    }

    @Test
    void unregisterOrderBookSubscriptionsRemovesSubscribedStockCodes() {
        registry.registerNewOrderBookSubscriptions(List.of("005930"));
        registry.unregisterOrderBookSubscriptions(List.of("005930"));

        List<String> result = registry.registerNewOrderBookSubscriptions(List.of("005930"));

        assertThat(result).containsExactly("005930");
    }
}
