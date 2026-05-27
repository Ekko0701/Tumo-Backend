package com.tumo.stock.controller;

import com.tumo.stock.adapter.out.sse.StockOrderBookSseEmitterRegistry;
import com.tumo.stock.adapter.out.sse.StockPriceSseEmitterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * iOS 클라이언트가 실시간 시세 SSE stream을 구독하기 위한 controller.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stocks")
@Tag(name = "Stock Realtime Stream", description = "종목 실시간 시세 SSE API")
public class StockRealtimeStreamController {

    /**
     * 실시간 가격 SSE 연결과 이벤트 전송을 관리하는 registry.
     */
    private final StockPriceSseEmitterRegistry stockPriceSseEmitterRegistry;

    /**
     * 실시간 호가 SSE 연결과 이벤트 전송을 관리하는 registry.
     */
    private final StockOrderBookSseEmitterRegistry stockOrderBookSseEmitterRegistry;

    /**
     * 실시간 체결가 SSE stream을 연결한다.
     *
     * @param stockCodes 실시간 체결가 이벤트를 수신할 종목 코드 목록
     * @return 실시간 체결가 이벤트를 수신할 SSE 연결
     */
    @GetMapping(value = "/realtime/prices/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "실시간 체결가 stream 연결", description = "iOS 클라이언트가 Backend에서 발행하는 실시간 체결가 이벤트를 전체 또는 지정 종목 기준으로 수신합니다.")
    public SseEmitter streamRealtimePrices(
            @RequestParam(required = false) List<String> stockCodes
    ) {
        return stockPriceSseEmitterRegistry.connect(stockCodes);
    }

    /**
     * 특정 종목의 실시간 호가 SSE stream을 연결한다.
     *
     * @param stockCode 실시간 호가 이벤트를 수신할 종목 코드
     * @return 실시간 호가 이벤트를 수신할 SSE 연결
     */
    @GetMapping(value = "/{stockCode}/realtime/order-book/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "실시간 호가 stream 연결", description = "iOS 클라이언트가 Backend에서 발행하는 특정 종목의 실시간 호가 이벤트를 수신합니다.")
    public SseEmitter streamRealtimeOrderBook(
            @PathVariable String stockCode
    ) {
        return stockOrderBookSseEmitterRegistry.connect(stockCode);
    }
}
