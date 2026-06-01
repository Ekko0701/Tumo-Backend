package com.tumo.stock.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.dto.StockMasterImportResponse;
import com.tumo.stock.dto.StockPageResponse;
import com.tumo.stock.dto.StockResponse;
import com.tumo.stock.service.master.StockMasterImportService;
import com.tumo.stock.service.master.StockMasterQueryService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockMasterControllerTest {

    @Mock
    private StockMasterImportService stockMasterImportService;

    @Mock
    private StockMasterQueryService stockMasterQueryService;

    @InjectMocks
    private StockMasterController stockMasterController;

    @Test
    void getStockMasters() {
        StockPageResponse expectedResponse = new StockPageResponse(
                List.of(new StockResponse(
                        "005930",
                        "삼성전자",
                        "KOSPI",
                        80000L,
                        LocalDateTime.of(2026, 6, 1, 10, 0)
                )),
                0,
                30,
                true
        );
        given(stockMasterQueryService.getStockMasters(Market.KOSPI, 0, 30)).willReturn(expectedResponse);

        StockPageResponse response = stockMasterController.getStockMasters(Market.KOSPI, 0, 30);

        assertThat(response).isEqualTo(expectedResponse);
        verify(stockMasterQueryService).getStockMasters(Market.KOSPI, 0, 30);
    }

    @Test
    void importStockMasters() {
        StockMasterImportResponse expectedResponse = new StockMasterImportResponse(10, 2, 3);
        given(stockMasterImportService.importStockMasters()).willReturn(expectedResponse);

        StockMasterImportResponse response = stockMasterController.importStockMasters();

        assertThat(response).isEqualTo(expectedResponse);
        verify(stockMasterImportService).importStockMasters();
    }
}
