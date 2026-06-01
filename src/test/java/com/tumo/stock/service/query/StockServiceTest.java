package com.tumo.stock.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.domain.price.StockPrice;
import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.dto.StockListResponse;
import com.tumo.stock.dto.StockResponse;
import com.tumo.stock.port.query.StockPriceQueryPort;
import com.tumo.stock.repository.StockRepository;
import java.math.BigDecimal;
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

    @Mock
    private StockPriceQueryPort stockPriceQueryPort;

    @InjectMocks
    private StockService stockService;

    @Test
    void getStocksRefreshesCurrentPrices() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 13, 15, 30);
        LocalDateTime refreshedAt = LocalDateTime.of(2026, 6, 1, 10, 0);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        Stock skHynix = new Stock("000660", "SK하이닉스", Market.KOSPI, 180000L, priceChangedAt);
        given(stockRepository.findAll()).willReturn(List.of(samsung, skHynix));
        given(stockPriceQueryPort.findCurrentPrice("005930"))
                .willReturn(Optional.of(new StockPrice(
                        "005930",
                        80000L,
                        5000L,
                        BigDecimal.valueOf(6.67),
                        1000000L,
                        refreshedAt
                )));
        given(stockPriceQueryPort.findCurrentPrice("000660"))
                .willReturn(Optional.of(new StockPrice(
                        "000660",
                        190000L,
                        10000L,
                        BigDecimal.valueOf(5.56),
                        500000L,
                        refreshedAt
                )));

        StockListResponse response = stockService.getStocks();

        assertThat(response.stocks()).hasSize(2);
        assertThat(response.stocks())
                .extracting(StockResponse::stockCode)
                .containsExactly("005930", "000660");
        assertThat(response.stocks())
                .extracting(StockResponse::currentPrice)
                .containsExactly(80000L, 190000L);
        assertThat(samsung.getCurrentPrice()).isEqualTo(80000L);
        assertThat(samsung.getPriceChangedAt()).isEqualTo(refreshedAt);
        assertThat(skHynix.getCurrentPrice()).isEqualTo(190000L);
        assertThat(skHynix.getPriceChangedAt()).isEqualTo(refreshedAt);
    }

    @Test
    void getStocksUsesStoredPriceWhenCurrentPriceIsEmpty() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 13, 15, 30);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        given(stockRepository.findAll()).willReturn(List.of(samsung));
        given(stockPriceQueryPort.findCurrentPrice("005930")).willReturn(Optional.empty());

        StockListResponse response = stockService.getStocks();

        assertThat(response.stocks()).hasSize(1);
        assertThat(response.stocks().getFirst().currentPrice()).isEqualTo(75000L);
        assertThat(response.stocks().getFirst().priceChangedAt()).isEqualTo(priceChangedAt);
        assertThat(samsung.getCurrentPrice()).isEqualTo(75000L);
        assertThat(samsung.getPriceChangedAt()).isEqualTo(priceChangedAt);
    }

    @Test
    void getStocksUsesStoredPriceWhenCurrentPriceQueryFails() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 13, 15, 30);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        given(stockRepository.findAll()).willReturn(List.of(samsung));
        given(stockPriceQueryPort.findCurrentPrice("005930")).willThrow(new RuntimeException("KIS error"));

        StockListResponse response = stockService.getStocks();

        assertThat(response.stocks()).hasSize(1);
        assertThat(response.stocks().getFirst().currentPrice()).isEqualTo(75000L);
        assertThat(response.stocks().getFirst().priceChangedAt()).isEqualTo(priceChangedAt);
        assertThat(samsung.getCurrentPrice()).isEqualTo(75000L);
        assertThat(samsung.getPriceChangedAt()).isEqualTo(priceChangedAt);
    }

    @Test
    void getStockRefreshesCurrentPrice() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 13, 15, 30);
        LocalDateTime refreshedAt = LocalDateTime.of(2026, 6, 1, 10, 0);
        Stock stock = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(stock));
        given(stockPriceQueryPort.findCurrentPrice("005930"))
                .willReturn(Optional.of(new StockPrice(
                        "005930",
                        80000L,
                        5000L,
                        BigDecimal.valueOf(6.67),
                        1000000L,
                        refreshedAt
                )));

        StockResponse response = stockService.getStock("005930");

        assertThat(response.stockCode()).isEqualTo("005930");
        assertThat(response.stockName()).isEqualTo("삼성전자");
        assertThat(response.market()).isEqualTo("KOSPI");
        assertThat(response.currentPrice()).isEqualTo(80000L);
        assertThat(response.priceChangedAt()).isEqualTo(refreshedAt);
        assertThat(stock.getCurrentPrice()).isEqualTo(80000L);
        assertThat(stock.getPriceChangedAt()).isEqualTo(refreshedAt);
    }

    @Test
    void getStockUsesStoredPriceWhenCurrentPriceQueryFails() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 5, 13, 15, 30);
        Stock stock = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt);
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(stock));
        given(stockPriceQueryPort.findCurrentPrice("005930")).willThrow(new RuntimeException("KIS error"));

        StockResponse response = stockService.getStock("005930");

        assertThat(response.stockCode()).isEqualTo("005930");
        assertThat(response.currentPrice()).isEqualTo(75000L);
        assertThat(response.priceChangedAt()).isEqualTo(priceChangedAt);
        assertThat(stock.getCurrentPrice()).isEqualTo(75000L);
        assertThat(stock.getPriceChangedAt()).isEqualTo(priceChangedAt);
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
