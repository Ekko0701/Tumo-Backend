package com.tumo.stock.controller.realtime;

import com.tumo.stock.dto.StockRealtimeSubscriptionResponse;
import com.tumo.stock.service.subscription.StockPriceSubscriptionService;
import com.tumo.stock.service.query.StockRealtimeSubscriptionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/stocks/realtime")
@Tag(name = "Stock Realtime", description = "종목 실시간 시세 내부 API")
public class StockRealtimeController {

    private final StockPriceSubscriptionService stockPriceSubscriptionService;
    private final StockRealtimeSubscriptionQueryService stockRealtimeSubscriptionQueryService;

    @GetMapping("/subscriptions")
    @Operation(summary = "실시간 시세 구독 상태 조회", description = "현재 Backend가 구독 중인 KIS 실시간 체결가/호가 종목 목록을 조회합니다.")
    public StockRealtimeSubscriptionResponse getSubscriptions() {
        return stockRealtimeSubscriptionQueryService.getSubscriptions();
    }

    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "실시간 체결가 구독 시작", description = "Backend에 등록된 모든 종목의 KIS 실시간 체결가 구독을 시작합니다.")
    public void subscribeRealtimePrices() {
        stockPriceSubscriptionService.subscribeAllStocks();
    }
}
