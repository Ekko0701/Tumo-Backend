package com.tumo.stock.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.tumo.stock.dto.StockRealtimeSubscriptionResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockRealtimeSubscriptionQueryServiceTest {

    @Mock
    private StockRealtimeSubscriptionRegistry stockRealtimeSubscriptionRegistry;

    @InjectMocks
    private StockRealtimeSubscriptionQueryService stockRealtimeSubscriptionQueryService;

    @Test
    void getSubscriptions() {
        given(stockRealtimeSubscriptionRegistry.getSubscribedPriceStockCodes())
                .willReturn(List.of("000660", "005930"));
        given(stockRealtimeSubscriptionRegistry.getSubscribedOrderBookStockCodes())
                .willReturn(List.of("005930"));

        StockRealtimeSubscriptionResponse response = stockRealtimeSubscriptionQueryService.getSubscriptions();

        assertThat(response.priceStockCodes()).containsExactly("000660", "005930");
        assertThat(response.orderBookStockCodes()).containsExactly("005930");
    }
}
