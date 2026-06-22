package com.tumo.stock.service.subscription;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.port.client.StockRealtimeOrderBookClient;
import com.tumo.stock.port.handler.StockOrderBookEventHandler;
import com.tumo.stock.repository.StockRepository;
import com.tumo.stock.service.realtime.StockOrderBookService;
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
class StockOrderBookSubscriptionServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockRealtimeOrderBookClient stockRealtimeOrderBookClient;

    @Mock
    private StockOrderBookService stockOrderBookService;

    @Mock
    private StockRealtimeSubscriptionRegistry stockRealtimeSubscriptionRegistry;

    @InjectMocks
    private StockOrderBookSubscriptionService stockOrderBookSubscriptionService;

    @Test
    void subscribeSubscribesSingleStockCode() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 25, 9, 0);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(samsung));
        given(stockRealtimeSubscriptionRegistry.acquireOrderBookSubscriptions(List.of("005930")))
                .willReturn(List.of("005930"));

        stockOrderBookSubscriptionService.subscribe("005930");

        verify(stockRealtimeOrderBookClient).subscribe(
                eq(List.of("005930")),
                any(StockOrderBookEventHandler.class)
        );
    }

    @Test
    void subscribeDoesNotSubscribeWhenStockCodeAlreadySubscribed() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 25, 9, 0);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(samsung));
        given(stockRealtimeSubscriptionRegistry.acquireOrderBookSubscriptions(List.of("005930")))
                .willReturn(List.of());

        stockOrderBookSubscriptionService.subscribe("005930");

        verify(stockRealtimeOrderBookClient, never()).subscribe(
                any(),
                any(StockOrderBookEventHandler.class)
        );
    }

    @Test
    void subscribeUnregistersStockCodeWhenSubscribeFails() {
        Stock samsung = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                75000L,
                LocalDateTime.of(2026, 5, 25, 9, 0)
        );
        RuntimeException exception = new IllegalStateException("KIS 구독 실패");
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(samsung));
        given(stockRealtimeSubscriptionRegistry.acquireOrderBookSubscriptions(List.of("005930")))
                .willReturn(List.of("005930"));
        org.mockito.BDDMockito.willThrow(exception)
                .given(stockRealtimeOrderBookClient)
                .subscribe(eq(List.of("005930")), any(StockOrderBookEventHandler.class));

        assertThatThrownBy(() -> stockOrderBookSubscriptionService.subscribe("005930"))
                .isEqualTo(exception);

        verify(stockRealtimeSubscriptionRegistry).releaseOrderBookSubscriptions(List.of("005930"));
    }

    @Test
    void unsubscribeUnsubscribesWhenLastReferenceIsGone() {
        given(stockRealtimeSubscriptionRegistry.releaseOrderBookSubscriptions(List.of("005930")))
                .willReturn(List.of("005930"));

        stockOrderBookSubscriptionService.unsubscribe("005930");

        verify(stockRealtimeOrderBookClient).unsubscribeOrderBook(List.of("005930"));
    }

    @Test
    void unsubscribeDoesNotCallClientWhenReferenceRemains() {
        given(stockRealtimeSubscriptionRegistry.releaseOrderBookSubscriptions(List.of("005930")))
                .willReturn(List.of());

        stockOrderBookSubscriptionService.unsubscribe("005930");

        verify(stockRealtimeOrderBookClient, never()).unsubscribeOrderBook(any());
    }

    @Test
    void unsubscribeDoesNothingWhenStockCodeIsNullOrBlank() {
        stockOrderBookSubscriptionService.unsubscribe(null);
        stockOrderBookSubscriptionService.unsubscribe(" ");

        verify(stockRealtimeOrderBookClient, never()).unsubscribeOrderBook(any());
    }

    @Test
    void subscribeThrowsExceptionWhenStockDoesNotExist() {
        given(stockRepository.findByStockCode("999999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> stockOrderBookSubscriptionService.subscribe("999999"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STOCK_NOT_FOUND)
                );

        verify(stockRealtimeOrderBookClient, never()).subscribe(
                any(),
                any(StockOrderBookEventHandler.class)
        );
    }
}
