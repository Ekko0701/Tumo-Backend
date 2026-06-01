package com.tumo.stock.controller.realtime;

import static org.mockito.Mockito.verify;

import com.tumo.stock.service.subscription.StockOrderBookSubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockRealtimeOrderBookControllerTest {

    @Mock
    private StockOrderBookSubscriptionService stockOrderBookSubscriptionService;

    @InjectMocks
    private StockRealtimeOrderBookController stockRealtimeOrderBookController;

    @Test
    void subscribeRealtimeOrderBook() {
        stockRealtimeOrderBookController.subscribeRealtimeOrderBook("005930");

        verify(stockOrderBookSubscriptionService).subscribe("005930");
    }
}
