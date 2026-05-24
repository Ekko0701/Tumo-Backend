package com.tumo.stock.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tumo.stock.domain.Market;
import com.tumo.stock.domain.Stock;
import com.tumo.stock.port.StockPriceEventHandler;
import com.tumo.stock.port.StockRealtimePriceClient;
import com.tumo.stock.repository.StockRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockPriceSubscriptionServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockRealtimePriceClient stockRealtimePriceClient;

    @Mock
    private StockRealtimePriceService stockRealtimePriceService;

    @InjectMocks
    private StockPriceSubscriptionService stockPriceSubscriptionService;

    @Test
    void subscribeAllStocksSubscribesRegisteredStockCodes() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 25, 9, 0);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        Stock skHynix = new Stock("000660", "SK하이닉스", Market.KOSPI, 180000L, priceChangedAt);
        given(stockRepository.findAll()).willReturn(List.of(samsung, skHynix));

        stockPriceSubscriptionService.subscribeAllStocks();

        verify(stockRealtimePriceClient).subscribe(
                eq(List.of("005930", "000660")),
                any(StockPriceEventHandler.class)
        );
    }

    @Test
    void subscribeAllStocksDoesNotSubscribeWhenStockDoesNotExist() {
        given(stockRepository.findAll()).willReturn(List.of());

        stockPriceSubscriptionService.subscribeAllStocks();

        verify(stockRealtimePriceClient, never()).subscribe(
                any(),
                any(StockPriceEventHandler.class)
        );
    }
}
