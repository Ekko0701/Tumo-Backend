package com.tumo.stock.controller.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tumo.stock.dto.StockRealtimeSubscriptionResponse;
import com.tumo.stock.service.subscription.StockOrderBookSubscriptionService;
import com.tumo.stock.service.subscription.StockPriceSubscriptionService;
import com.tumo.stock.service.query.StockRealtimeSubscriptionQueryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockRealtimeControllerTest {

    @Mock
    private StockPriceSubscriptionService stockPriceSubscriptionService;

    @Mock
    private StockOrderBookSubscriptionService stockOrderBookSubscriptionService;

    @Mock
    private StockRealtimeSubscriptionQueryService stockRealtimeSubscriptionQueryService;

    @InjectMocks
    private StockRealtimeController stockRealtimeController;

    @Test
    void getSubscriptions() {
        StockRealtimeSubscriptionResponse expectedResponse = new StockRealtimeSubscriptionResponse(
                List.of("005930"),
                List.of("000660")
        );
        given(stockRealtimeSubscriptionQueryService.getSubscriptions()).willReturn(expectedResponse);

        StockRealtimeSubscriptionResponse response = stockRealtimeController.getSubscriptions();

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void subscribeRealtimePrices() {
        stockRealtimeController.subscribeRealtimePrices();

        verify(stockPriceSubscriptionService).subscribeAllStocks();
    }

    @Test
    void subscribeRealtimeOrderBooks() {
        stockRealtimeController.subscribeRealtimeOrderBooks();

        verify(stockOrderBookSubscriptionService).subscribeAllStocks();
    }
}
