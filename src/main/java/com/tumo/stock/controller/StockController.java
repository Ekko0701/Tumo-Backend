package com.tumo.stock.controller;

import com.tumo.stock.domain.ranking.StockRankingType;
import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.dto.StockPageResponse;
import com.tumo.stock.dto.StockResponse;
import com.tumo.stock.service.ranking.StockRankingService;
import com.tumo.stock.service.query.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stocks")
@Tag(name = "Stock", description = "종목 API")
public class StockController {

    private final StockService stockService;
    private final StockRankingService stockRankingService;

    @GetMapping
    @Operation(summary = "종목 목록 조회", description = "시장별 종목 목록을 page 단위로 조회합니다.")
    public StockPageResponse getStocks(
            @RequestParam Market market,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        return stockService.getStocks(market, page, size);
    }

    @GetMapping("/rankings")
    @Operation(summary = "종목 랭킹 목록 조회", description = "시장과 랭킹 기준에 해당하는 종목 목록을 page 단위로 조회합니다.")
    public StockPageResponse getStockRankings(
            @RequestParam Market market,
            @RequestParam StockRankingType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        return stockRankingService.getRankings(market, type, page, size);
    }

    @GetMapping("/{stockCode}")
    @Operation(summary = "종목 상세 조회", description = "종목 코드로 특정 종목의 현재가 정보를 조회합니다.")
    public StockResponse getStock(@PathVariable String stockCode) {
        return stockService.getStock(stockCode);
    }
}
