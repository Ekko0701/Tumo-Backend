package com.tumo.stock.controller;

import com.tumo.stock.dto.StockMasterImportResponse;
import com.tumo.stock.service.master.StockMasterImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/stocks/master")
@Tag(name = "Stock Master", description = "종목 마스터 내부 API")
public class StockMasterController {

    private final StockMasterImportService stockMasterImportService;

    @PostMapping("/import")
    @Operation(summary = "종목 마스터 import", description = "KIS 공식 KOSPI/KOSDAQ 종목 마스터 파일을 다운로드해 일반 주식 종목을 DB에 반영합니다.")
    public StockMasterImportResponse importStockMasters() {
        return stockMasterImportService.importStockMasters();
    }
}
