package com.tumo.stock.controller.realtime;

import com.tumo.stock.service.subscription.StockOrderBookSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/stocks/{stockCode}/realtime/order-book")
@Tag(name = "Stock Realtime", description = "종목 실시간 시세 내부 API")
public class StockRealtimeOrderBookController {

    private final StockOrderBookSubscriptionService stockOrderBookSubscriptionService;

    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "실시간 호가 구독 시작", description = "요청한 단일 종목의 KIS 실시간 호가 구독을 시작합니다.")
    public void subscribeRealtimeOrderBook(@PathVariable String stockCode) {
        stockOrderBookSubscriptionService.subscribe(stockCode);
    }
}
