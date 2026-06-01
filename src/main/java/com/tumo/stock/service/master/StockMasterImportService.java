package com.tumo.stock.service.master;

import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.dto.StockMasterImportResponse;
import com.tumo.stock.port.client.StockMasterFile;
import com.tumo.stock.port.client.StockMasterFileClient;
import com.tumo.stock.repository.StockRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * KIS 종목 마스터 파일을 읽어 Backend 종목 DB에 반영하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class StockMasterImportService {

    private final StockMasterFileClient stockMasterFileClient;
    private final StockMasterFileParser stockMasterFileParser;
    private final StockRepository stockRepository;

    /**
     * KIS 공식 KOSPI/KOSDAQ 종목 마스터 파일을 import한다.
     *
     * @return 종목 마스터 import 결과
     */
    @Transactional
    public StockMasterImportResponse importStockMasters() {
        int importedCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (StockMasterFile stockMasterFile : stockMasterFileClient.downloadStockMasterFiles()) {
            StockMasterParseResult parseResult = stockMasterFileParser.parse(stockMasterFile);
            skippedCount += parseResult.skippedCount();

            for (StockMasterInfo stockMaster : parseResult.stockMasters()) {
                Stock stock = stockRepository.findByStockCode(stockMaster.stockCode())
                        .orElse(null);

                if (stock == null) {
                    stockRepository.save(new Stock(
                            stockMaster.stockCode(),
                            stockMaster.stockName(),
                            stockMaster.market(),
                            stockMaster.basePrice(),
                            now
                    ));
                    importedCount++;
                    continue;
                }

                stock.updateMasterInfo(stockMaster.stockName(), stockMaster.market());
                updatedCount++;
            }
        }

        return new StockMasterImportResponse(importedCount, updatedCount, skippedCount);
    }
}
