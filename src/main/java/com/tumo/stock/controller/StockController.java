package com.tumo.stock.controller;

import com.tumo.stock.dto.StockListResponse;
import com.tumo.stock.dto.StockResponse;
import com.tumo.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stocks")
@Tag(name = "Stock", description = "종목 API")
public class StockController {

    private final StockService stockService;

    @GetMapping
    @Operation(summary = "종목 목록 조회", description = "거래 가능한 종목 목록을 조회합니다.")
    public StockListResponse getStocks() {
        return stockService.getStocks();
    }

    @GetMapping("/{stockCode}")
    @Operation(summary = "종목 상세 조회", description = "종목 코드로 특정 종목의 현재가 정보를 조회합니다.")
    public StockResponse getStock(@PathVariable String stockCode) {
        return stockService.getStock(stockCode);
    }
}
