package com.tumo.stock.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.domain.Market;
import com.tumo.stock.domain.Stock;
import com.tumo.stock.dto.StockListResponse;
import com.tumo.stock.dto.StockResponse;
import com.tumo.stock.repository.StockRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    @Test
    void getStocks() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 13, 15, 30);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        Stock skHynix = new Stock("000660", "SK하이닉스", Market.KOSPI, 180000L, priceChangedAt);
        given(stockRepository.findAll()).willReturn(List.of(samsung, skHynix));

        StockListResponse response = stockService.getStocks();

        assertThat(response.stocks()).hasSize(2);
        assertThat(response.stocks())
                .extracting(StockResponse::stockCode)
                .containsExactly("005930", "000660");
    }

    @Test
    void getStock() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 13, 15, 30);
        Stock stock = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(stock));

        StockResponse response = stockService.getStock("005930");

        assertThat(response.stockCode()).isEqualTo("005930");
        assertThat(response.stockName()).isEqualTo("삼성전자");
        assertThat(response.market()).isEqualTo("KOSPI");
        assertThat(response.currentPrice()).isEqualTo(75000L);
        assertThat(response.priceChangedAt()).isEqualTo(priceChangedAt);
    }

    @Test
    void getStockThrowsExceptionWhenStockDoesNotExist() {
        given(stockRepository.findByStockCode("999999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.getStock("999999"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STOCK_NOT_FOUND)
                );
    }
}
