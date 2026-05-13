package com.tumo.holding.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tumo.stock.domain.Market;
import com.tumo.stock.domain.Stock;
import com.tumo.user.domain.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class HoldingTest {

    @Test
    void buyUpdatesQuantityAndAveragePrice() {
        User user = new User("test@example.com", "encoded-password", "tester");
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                80000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        Holding holding = new Holding(user, stock, 10L, 70000L);

        holding.buy(5L, 80000L);

        assertThat(holding.getQuantity()).isEqualTo(15L);
        assertThat(holding.getAveragePrice()).isEqualTo(73333L);
    }
}
