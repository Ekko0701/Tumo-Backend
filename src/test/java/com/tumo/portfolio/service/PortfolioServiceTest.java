package com.tumo.portfolio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.holding.domain.Holding;
import com.tumo.holding.repository.HoldingRepository;
import com.tumo.portfolio.dto.PortfolioResponse;
import com.tumo.stock.domain.Market;
import com.tumo.stock.domain.Stock;
import com.tumo.user.domain.User;
import com.tumo.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    void getMyPortfolio() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        user.decreaseCashBalance(700000L);
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                80000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        Holding holding = new Holding(user, stock, 10L, 70000L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(holdingRepository.findAllByUser(user)).willReturn(List.of(holding));

        PortfolioResponse response = portfolioService.getMyPortfolio(1L);

        assertThat(response.cashBalance()).isEqualTo(9300000L);
        assertThat(response.totalStockValue()).isEqualTo(800000L);
        assertThat(response.totalAsset()).isEqualTo(10100000L);
        assertThat(response.profitAmount()).isEqualTo(100000L);
        assertThat(response.profitRate()).isEqualTo(1.0);
        assertThat(response.holdings()).hasSize(1);
        assertThat(response.holdings().get(0).stockCode()).isEqualTo("005930");
        assertThat(response.holdings().get(0).evaluationAmount()).isEqualTo(800000L);
        assertThat(response.holdings().get(0).profitAmount()).isEqualTo(100000L);
        assertThat(response.holdings().get(0).profitRate()).isEqualTo(14.285714285714286);
    }

    @Test
    void getMyPortfolioWhenHoldingDoesNotExist() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(holdingRepository.findAllByUser(user)).willReturn(List.of());

        PortfolioResponse response = portfolioService.getMyPortfolio(1L);

        assertThat(response.cashBalance()).isEqualTo(10000000L);
        assertThat(response.totalStockValue()).isZero();
        assertThat(response.totalAsset()).isEqualTo(10000000L);
        assertThat(response.profitAmount()).isZero();
        assertThat(response.profitRate()).isZero();
        assertThat(response.holdings()).isEmpty();
    }

    @Test
    void getMyPortfolioThrowsExceptionWhenUserDoesNotExist() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.getMyPortfolio(1L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND)
                );
    }
}
