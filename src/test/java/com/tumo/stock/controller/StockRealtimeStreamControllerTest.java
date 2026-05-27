package com.tumo.stock.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tumo.stock.adapter.out.sse.StockOrderBookSseEmitterRegistry;
import com.tumo.stock.adapter.out.sse.StockPriceSseEmitterRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class StockRealtimeStreamControllerTest {

    @Mock
    private StockPriceSseEmitterRegistry stockPriceSseEmitterRegistry;

    @Mock
    private StockOrderBookSseEmitterRegistry stockOrderBookSseEmitterRegistry;

    @InjectMocks
    private StockRealtimeStreamController stockRealtimeStreamController;

    @Test
    void streamRealtimePrices() {
        SseEmitter expectedEmitter = new SseEmitter();
        List<String> stockCodes = List.of("005930", "000660");
        given(stockPriceSseEmitterRegistry.connect(stockCodes)).willReturn(expectedEmitter);

        SseEmitter emitter = stockRealtimeStreamController.streamRealtimePrices(stockCodes);

        assertThat(emitter).isEqualTo(expectedEmitter);
        verify(stockPriceSseEmitterRegistry).connect(stockCodes);
    }

    @Test
    void streamRealtimeOrderBook() {
        SseEmitter expectedEmitter = new SseEmitter();
        given(stockOrderBookSseEmitterRegistry.connect("005930")).willReturn(expectedEmitter);

        SseEmitter emitter = stockRealtimeStreamController.streamRealtimeOrderBook("005930");

        assertThat(emitter).isEqualTo(expectedEmitter);
        verify(stockOrderBookSseEmitterRegistry).connect("005930");
    }
}
