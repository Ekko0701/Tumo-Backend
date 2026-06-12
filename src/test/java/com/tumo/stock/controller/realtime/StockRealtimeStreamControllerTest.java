package com.tumo.stock.controller.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tumo.stock.adapter.out.sse.orderbook.StockOrderBookSseEmitterRegistry;
import com.tumo.stock.adapter.out.sse.price.StockPriceSseEmitterRegistry;
import com.tumo.stock.service.subscription.StockOrderBookSubscriptionService;
import com.tumo.stock.service.subscription.StockPriceSubscriptionService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class StockRealtimeStreamControllerTest {

    @Mock
    private StockPriceSseEmitterRegistry stockPriceSseEmitterRegistry;

    @Mock
    private StockOrderBookSseEmitterRegistry stockOrderBookSseEmitterRegistry;

    @Mock
    private StockOrderBookSubscriptionService stockOrderBookSubscriptionService;

    @Mock
    private StockPriceSubscriptionService stockPriceSubscriptionService;

    @InjectMocks
    private StockRealtimeStreamController stockRealtimeStreamController;

    @Test
    void streamRealtimePrices() {
        SseEmitter expectedEmitter = new SseEmitter();
        List<String> stockCodes = List.of("005930", "000660");
        given(stockPriceSseEmitterRegistry.connect(stockCodes)).willReturn(expectedEmitter);

        SseEmitter emitter = stockRealtimeStreamController.streamRealtimePrices(stockCodes);

        assertThat(emitter).isEqualTo(expectedEmitter);
        InOrder inOrder = Mockito.inOrder(stockPriceSubscriptionService, stockPriceSseEmitterRegistry);
        inOrder.verify(stockPriceSubscriptionService).subscribe(stockCodes);
        inOrder.verify(stockPriceSseEmitterRegistry).connect(stockCodes);
    }

    @Test
    void streamRealtimeOrderBook() {
        SseEmitter expectedEmitter = new SseEmitter();
        given(stockOrderBookSseEmitterRegistry.connect("005930")).willReturn(expectedEmitter);

        SseEmitter emitter = stockRealtimeStreamController.streamRealtimeOrderBook("005930");

        assertThat(emitter).isEqualTo(expectedEmitter);
        InOrder inOrder = Mockito.inOrder(stockOrderBookSubscriptionService, stockOrderBookSseEmitterRegistry);
        inOrder.verify(stockOrderBookSubscriptionService).subscribe("005930");
        inOrder.verify(stockOrderBookSseEmitterRegistry).connect("005930");
    }
}
