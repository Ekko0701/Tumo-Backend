package com.tumo.stock.adapter.out.sse.heartbeat;

import static org.mockito.Mockito.verify;

import com.tumo.stock.adapter.out.sse.orderbook.StockOrderBookSseEmitterRegistry;
import com.tumo.stock.adapter.out.sse.price.StockPriceSseEmitterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockRealtimeSseHeartbeatSchedulerTest {

    @Mock
    private StockPriceSseEmitterRegistry stockPriceSseEmitterRegistry;

    @Mock
    private StockOrderBookSseEmitterRegistry stockOrderBookSseEmitterRegistry;

    @InjectMocks
    private StockRealtimeSseHeartbeatScheduler stockRealtimeSseHeartbeatScheduler;

    @Test
    void sendHeartbeat() {
        stockRealtimeSseHeartbeatScheduler.sendHeartbeat();

        verify(stockPriceSseEmitterRegistry).sendHeartbeat();
        verify(stockOrderBookSseEmitterRegistry).sendHeartbeat();
    }
}
