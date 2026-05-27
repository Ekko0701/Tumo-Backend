package com.tumo.stock.adapter.out.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 실시간 시세 SSE 연결에 heartbeat 이벤트를 주기적으로 전송하는 scheduler.
 */
@Component
@RequiredArgsConstructor
public class StockRealtimeSseHeartbeatScheduler {

    private static final long HEARTBEAT_INTERVAL_MILLIS = 30 * 1000L;

    /**
     * 실시간 가격 SSE 연결과 이벤트 전송을 관리하는 registry.
     */
    private final StockPriceSseEmitterRegistry stockPriceSseEmitterRegistry;

    /**
     * 실시간 호가 SSE 연결과 이벤트 전송을 관리하는 registry.
     */
    private final StockOrderBookSseEmitterRegistry stockOrderBookSseEmitterRegistry;

    /**
     * 가격/호가 SSE 연결에 heartbeat 이벤트를 전송한다.
     */
    @Scheduled(fixedDelay = HEARTBEAT_INTERVAL_MILLIS)
    public void sendHeartbeat() {
        stockPriceSseEmitterRegistry.sendHeartbeat();
        stockOrderBookSseEmitterRegistry.sendHeartbeat();
    }
}
