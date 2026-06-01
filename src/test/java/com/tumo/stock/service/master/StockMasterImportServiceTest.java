package com.tumo.stock.service.master;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.dto.StockMasterImportResponse;
import com.tumo.stock.port.client.StockMasterFile;
import com.tumo.stock.port.client.StockMasterFileClient;
import com.tumo.stock.repository.StockRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockMasterImportServiceTest {

    @Mock
    private StockMasterFileClient stockMasterFileClient;

    @Mock
    private StockMasterFileParser stockMasterFileParser;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockMasterImportService stockMasterImportService;

    @Test
    void importStockMasters() {
        StockMasterFile stockMasterFile = new StockMasterFile(Market.KOSPI, new byte[]{1, 2, 3});
        Stock existingStock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                75000L,
                LocalDateTime.of(2026, 5, 31, 15, 30)
        );
        given(stockMasterFileClient.downloadStockMasterFiles()).willReturn(List.of(stockMasterFile));
        given(stockMasterFileParser.parse(stockMasterFile)).willReturn(new StockMasterParseResult(
                List.of(
                        new StockMasterInfo("005930", "삼성전자보통주", Market.KOSPI, 80000L),
                        new StockMasterInfo("000660", "SK하이닉스", Market.KOSPI, 180000L)
                ),
                3
        ));
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(existingStock));
        given(stockRepository.findByStockCode("000660")).willReturn(Optional.empty());
        ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);

        StockMasterImportResponse response = stockMasterImportService.importStockMasters();

        assertThat(response.importedCount()).isEqualTo(1);
        assertThat(response.updatedCount()).isEqualTo(1);
        assertThat(response.skippedCount()).isEqualTo(3);
        assertThat(existingStock.getStockName()).isEqualTo("삼성전자보통주");
        assertThat(existingStock.getMarket()).isEqualTo(Market.KOSPI);
        assertThat(existingStock.getCurrentPrice()).isEqualTo(75000L);
        verify(stockRepository).save(stockCaptor.capture());
        Stock savedStock = stockCaptor.getValue();
        assertThat(savedStock.getStockCode()).isEqualTo("000660");
        assertThat(savedStock.getStockName()).isEqualTo("SK하이닉스");
        assertThat(savedStock.getMarket()).isEqualTo(Market.KOSPI);
        assertThat(savedStock.getCurrentPrice()).isEqualTo(180000L);
    }
}
