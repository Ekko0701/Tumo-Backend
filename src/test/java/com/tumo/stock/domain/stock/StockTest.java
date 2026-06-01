package com.tumo.stock.domain.stock;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class StockTest {

    @Test
    void updateMasterInfo() {
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                75000L,
                LocalDateTime.of(2026, 5, 31, 15, 30)
        );

        stock.updateMasterInfo("삼성전자보통주", Market.KOSPI);

        assertThat(stock.getStockName()).isEqualTo("삼성전자보통주");
        assertThat(stock.getMarket()).isEqualTo(Market.KOSPI);
        assertThat(stock.getCurrentPrice()).isEqualTo(75000L);
    }
}
