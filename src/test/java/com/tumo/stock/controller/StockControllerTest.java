package com.tumo.stock.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tumo.stock.domain.ranking.StockRankingType;
import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.dto.StockPageResponse;
import com.tumo.stock.dto.StockResponse;
import com.tumo.stock.service.ranking.StockRankingService;
import com.tumo.stock.service.query.StockService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    @Mock
    private StockService stockService;

    @Mock
    private StockRankingService stockRankingService;

    @InjectMocks
    private StockController stockController;

    @Test
    void getStocks() {
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
        given(stockService.getStocks(Market.KOSPI, 0, 30)).willReturn(expectedResponse);

        StockPageResponse response = stockController.getStocks(Market.KOSPI, 0, 30);

        assertThat(response).isEqualTo(expectedResponse);
        verify(stockService).getStocks(Market.KOSPI, 0, 30);
    }

    @Test
    void getStockRankings() {
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
        given(stockRankingService.getRankings(Market.KOSPI, StockRankingType.TRADE_AMOUNT, 0, 30))
                .willReturn(expectedResponse);

        StockPageResponse response = stockController.getStockRankings(
                Market.KOSPI,
                StockRankingType.TRADE_AMOUNT,
                0,
                30
        );

        assertThat(response).isEqualTo(expectedResponse);
        verify(stockRankingService).getRankings(Market.KOSPI, StockRankingType.TRADE_AMOUNT, 0, 30);
    }
}
