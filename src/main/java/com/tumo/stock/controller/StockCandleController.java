package com.tumo.stock.controller;

import com.tumo.stock.domain.candle.CandleInterval;
import com.tumo.stock.dto.StockCandleListResponse;
import com.tumo.stock.service.query.StockCandleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stocks")
@Tag(name = "Stock Candle", description = "종목 캔들(차트) API")
public class StockCandleController {

    private final StockCandleService stockCandleService;

    @GetMapping("/{stockCode}/candles")
    @Operation(
            summary = "종목 캔들(차트) 조회",
            description = "종목 코드, 시간 단위(분/일/주/월/년), 기간으로 캔들(OHLCV) 목록을 조회합니다."
    )
    public StockCandleListResponse getCandles(
            @PathVariable String stockCode,
            @RequestParam CandleInterval interval,
            @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate from,
            @RequestParam @DateTimeFormat(pattern = "yyyyMMdd") LocalDate to
    ) {
        return stockCandleService.getCandles(stockCode, interval, from, to);
    }
}
