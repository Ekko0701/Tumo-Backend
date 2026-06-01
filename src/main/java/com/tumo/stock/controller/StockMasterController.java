package com.tumo.stock.controller;

import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.dto.StockMasterImportResponse;
import com.tumo.stock.dto.StockPageResponse;
import com.tumo.stock.service.master.StockMasterImportService;
import com.tumo.stock.service.master.StockMasterQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/stocks/master")
@Tag(name = "Stock Master", description = "종목 마스터 내부 API")
public class StockMasterController {

    private final StockMasterImportService stockMasterImportService;
    private final StockMasterQueryService stockMasterQueryService;

    @GetMapping
    @Operation(
            summary = "종목 마스터 조회",
            description = "DB에 저장된 시장별 종목 마스터 정보를 page 단위로 조회합니다. KIS 현재가 API를 호출하지 않고 DB에 저장된 가격을 그대로 반환합니다."
    )
    public StockPageResponse getStockMasters(
            @RequestParam Market market,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        return stockMasterQueryService.getStockMasters(market, page, size);
    }

    @PostMapping("/import")
    @Operation(summary = "종목 마스터 import", description = "KIS 공식 KOSPI/KOSDAQ 종목 마스터 파일을 다운로드해 일반 주식 종목을 DB에 반영합니다.")
    public StockMasterImportResponse importStockMasters() {
        return stockMasterImportService.importStockMasters();
    }
}
