package com.tumo.holding.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.domain.stock.Stock;
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

    @Test
    void sellReducesQuantityAndKeepsAveragePrice() {
        User user = new User("test@example.com", "encoded-password", "tester");
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                80000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        Holding holding = new Holding(user, stock, 10L, 70000L);

        holding.sell(4L);

        assertThat(holding.getQuantity()).isEqualTo(6L);
        assertThat(holding.getAveragePrice()).isEqualTo(70000L);
    }

    @Test
    void sellThrowsExceptionWhenQuantityExceedsHolding() {
        User user = new User("test@example.com", "encoded-password", "tester");
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                80000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        Holding holding = new Holding(user, stock, 3L, 70000L);

        assertThatThrownBy(() -> holding.sell(5L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_HOLDING)
                );

        assertThat(holding.getQuantity()).isEqualTo(3L);
    }
}
