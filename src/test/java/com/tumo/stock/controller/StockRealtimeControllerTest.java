package com.tumo.stock.controller;

import static org.mockito.Mockito.verify;

import com.tumo.stock.service.StockOrderBookSubscriptionService;
import com.tumo.stock.service.StockPriceSubscriptionService;
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

    @InjectMocks
    private StockRealtimeController stockRealtimeController;

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
