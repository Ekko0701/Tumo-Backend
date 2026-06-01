package com.tumo.stock.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tumo.stock.dto.StockMasterImportResponse;
import com.tumo.stock.service.master.StockMasterImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockMasterControllerTest {

    @Mock
    private StockMasterImportService stockMasterImportService;

    @InjectMocks
    private StockMasterController stockMasterController;

    @Test
    void importStockMasters() {
        StockMasterImportResponse expectedResponse = new StockMasterImportResponse(10, 2, 3);
        given(stockMasterImportService.importStockMasters()).willReturn(expectedResponse);

        StockMasterImportResponse response = stockMasterController.importStockMasters();

        assertThat(response).isEqualTo(expectedResponse);
        verify(stockMasterImportService).importStockMasters();
    }
}
