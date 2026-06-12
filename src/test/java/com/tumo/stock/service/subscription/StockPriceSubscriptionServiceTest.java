package com.tumo.stock.service.subscription;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tumo.global.error.BusinessException;
import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.port.client.StockRealtimePriceClient;
import com.tumo.stock.port.handler.StockPriceEventHandler;
import com.tumo.stock.repository.StockRepository;
import com.tumo.stock.service.realtime.StockRealtimePriceService;
import com.tumo.stock.service.registry.StockRealtimeSubscriptionRegistry;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    @Mock
    private StockRealtimeSubscriptionRegistry stockRealtimeSubscriptionRegistry;

    @InjectMocks
    private StockPriceSubscriptionService stockPriceSubscriptionService;

    @Test
    void subscribeSubscribesRequestedStockCodes() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 25, 9, 0);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        Stock skHynix = new Stock("000660", "SK하이닉스", Market.KOSPI, 180000L, priceChangedAt);
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(samsung));
        given(stockRepository.findByStockCode("000660")).willReturn(Optional.of(skHynix));
        given(stockRealtimeSubscriptionRegistry.registerNewPriceSubscriptions(List.of("005930", "000660")))
                .willReturn(List.of("005930", "000660"));

        stockPriceSubscriptionService.subscribe(List.of("005930", "000660"));

        verify(stockRealtimePriceClient).subscribe(
                eq(List.of("005930", "000660")),
                any(StockPriceEventHandler.class)
        );
    }

    @Test
    void subscribeThrowsWhenStockDoesNotExist() {
        given(stockRepository.findByStockCode("999999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> stockPriceSubscriptionService.subscribe(List.of("999999")))
                .isInstanceOf(BusinessException.class);

        verify(stockRealtimePriceClient, never()).subscribe(
                any(),
                any(StockPriceEventHandler.class)
        );
    }

    @Test
    void subscribeDoesNotSubscribeWhenAllStockCodesAlreadySubscribed() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 25, 9, 0);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(samsung));
        given(stockRealtimeSubscriptionRegistry.registerNewPriceSubscriptions(List.of("005930")))
                .willReturn(List.of());

        stockPriceSubscriptionService.subscribe(List.of("005930"));

        verify(stockRealtimePriceClient, never()).subscribe(
                any(),
                any(StockPriceEventHandler.class)
        );
    }

    @Test
    void subscribeDoesNothingWhenStockCodesIsNullOrEmpty() {
        stockPriceSubscriptionService.subscribe(null);
        stockPriceSubscriptionService.subscribe(List.of());

        verify(stockRealtimePriceClient, never()).subscribe(
                any(),
                any(StockPriceEventHandler.class)
        );
    }

    @Test
    void subscribeUnregistersStockCodesWhenClientSubscribeFails() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 25, 9, 0);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(samsung));
        given(stockRealtimeSubscriptionRegistry.registerNewPriceSubscriptions(List.of("005930")))
                .willReturn(List.of("005930"));
        willThrow(new RuntimeException("KIS 연결 실패"))
                .given(stockRealtimePriceClient)
                .subscribe(eq(List.of("005930")), any(StockPriceEventHandler.class));

        assertThatThrownBy(() -> stockPriceSubscriptionService.subscribe(List.of("005930")))
                .isInstanceOf(RuntimeException.class);

        verify(stockRealtimeSubscriptionRegistry).unregisterPriceSubscriptions(List.of("005930"));
    }

    @Test
    void subscribeAllStocksSubscribesRegisteredStockCodes() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 25, 9, 0);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        Stock skHynix = new Stock("000660", "SK하이닉스", Market.KOSPI, 180000L, priceChangedAt);
        given(stockRepository.findAll()).willReturn(List.of(samsung, skHynix));
        given(stockRealtimeSubscriptionRegistry.registerNewPriceSubscriptions(List.of("005930", "000660")))
                .willReturn(List.of("005930", "000660"));

        stockPriceSubscriptionService.subscribeAllStocks();

        verify(stockRealtimePriceClient).subscribe(
                eq(List.of("005930", "000660")),
                any(StockPriceEventHandler.class)
        );
    }

    @Test
    void subscribeAllStocksSubscribesOnlyNewStockCodes() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 25, 9, 0);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        Stock skHynix = new Stock("000660", "SK하이닉스", Market.KOSPI, 180000L, priceChangedAt);
        Stock naver = new Stock("035420", "NAVER", Market.KOSPI, 190000L, priceChangedAt);
        given(stockRepository.findAll()).willReturn(List.of(samsung, skHynix, naver));
        given(stockRealtimeSubscriptionRegistry.registerNewPriceSubscriptions(List.of("005930", "000660", "035420")))
                .willReturn(List.of("035420"));

        stockPriceSubscriptionService.subscribeAllStocks();

        verify(stockRealtimePriceClient).subscribe(
                eq(List.of("035420")),
                any(StockPriceEventHandler.class)
        );
    }

    @Test
    void subscribeAllStocksDoesNotSubscribeWhenAllStocksAlreadySubscribed() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 25, 9, 0);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        given(stockRepository.findAll()).willReturn(List.of(samsung));
        given(stockRealtimeSubscriptionRegistry.registerNewPriceSubscriptions(List.of("005930")))
                .willReturn(List.of());

        stockPriceSubscriptionService.subscribeAllStocks();

        verify(stockRealtimePriceClient, never()).subscribe(
                any(),
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
