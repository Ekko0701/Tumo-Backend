package com.tumo.stock.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tumo.stock.domain.candle.CandleInterval;
import com.tumo.stock.dto.StockCandleListResponse;
import com.tumo.stock.service.query.StockCandleService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockCandleControllerTest {

    @Mock
    private StockCandleService stockCandleService;

    @InjectMocks
    private StockCandleController stockCandleController;

    @Test
    void getCandlesDelegatesToService() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);
        StockCandleListResponse expected = new StockCandleListResponse("005930", "DAY", List.of());
        given(stockCandleService.getCandles("005930", CandleInterval.DAY, from, to)).willReturn(expected);

        StockCandleListResponse response =
                stockCandleController.getCandles("005930", CandleInterval.DAY, from, to);

        assertThat(response).isEqualTo(expected);
        verify(stockCandleService).getCandles("005930", CandleInterval.DAY, from, to);
    }
}
