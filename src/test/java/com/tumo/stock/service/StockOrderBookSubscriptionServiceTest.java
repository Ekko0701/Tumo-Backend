package com.tumo.stock.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.port.StockOrderBookEventHandler;
import com.tumo.stock.port.StockRealtimeOrderBookClient;
import com.tumo.stock.repository.StockRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockOrderBookSubscriptionServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockRealtimeOrderBookClient stockRealtimeOrderBookClient;

    @Mock
    private StockOrderBookService stockOrderBookService;

    @InjectMocks
    private StockOrderBookSubscriptionService stockOrderBookSubscriptionService;

    @Test
    void subscribeAllStocksSubscribesRegisteredStockCodes() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 25, 9, 0);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        Stock skHynix = new Stock("000660", "SK하이닉스", Market.KOSPI, 180000L, priceChangedAt);
        given(stockRepository.findAll()).willReturn(List.of(samsung, skHynix));

        stockOrderBookSubscriptionService.subscribeAllStocks();

        verify(stockRealtimeOrderBookClient).subscribe(
                eq(List.of("005930", "000660")),
                any(StockOrderBookEventHandler.class)
        );
    }

    @Test
    void subscribeAllStocksDoesNotSubscribeWhenStockDoesNotExist() {
        given(stockRepository.findAll()).willReturn(List.of());

        stockOrderBookSubscriptionService.subscribeAllStocks();

        verify(stockRealtimeOrderBookClient, never()).subscribe(
                any(),
                any(StockOrderBookEventHandler.class)
        );
    }
}
