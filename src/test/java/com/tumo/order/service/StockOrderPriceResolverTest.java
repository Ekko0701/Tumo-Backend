package com.tumo.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.domain.price.StockPrice;
import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.port.query.StockPriceQueryPort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockOrderPriceResolverTest {

    @Mock
    private StockPriceQueryPort stockPriceQueryPort;

    @InjectMocks
    private StockOrderPriceResolver stockOrderPriceResolver;

    @Test
    void resolveUpdatesStockPriceAndReturnsCurrentPrice() {
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                75000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 6, 1, 9, 30);
        given(stockPriceQueryPort.findCurrentPrice("005930"))
                .willReturn(Optional.of(new StockPrice(
                        "005930",
                        80000L,
                        5000L,
                        BigDecimal.valueOf(6.67),
                        1_234_567L,
                        priceChangedAt
                )));

        Long currentPrice = stockOrderPriceResolver.resolve(stock);

        assertThat(currentPrice).isEqualTo(80000L);
        assertThat(stock.getCurrentPrice()).isEqualTo(80000L);
        assertThat(stock.getPriceChangedAt()).isEqualTo(priceChangedAt);
    }

    @Test
    void resolveThrowsExceptionWhenCurrentPriceIsUnavailable() {
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                75000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        given(stockPriceQueryPort.findCurrentPrice("005930")).willReturn(Optional.empty());

        assertThatThrownBy(() -> stockOrderPriceResolver.resolve(stock))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STOCK_PRICE_UNAVAILABLE)
                );
    }
}
