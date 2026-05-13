package com.tumo.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.holding.domain.Holding;
import com.tumo.holding.repository.HoldingRepository;
import com.tumo.order.domain.Order;
import com.tumo.order.domain.OrderType;
import com.tumo.order.dto.OrderRequest;
import com.tumo.order.dto.OrderResponse;
import com.tumo.order.repository.OrderRepository;
import com.tumo.stock.domain.Market;
import com.tumo.stock.domain.Stock;
import com.tumo.stock.repository.StockRepository;
import com.tumo.user.domain.User;
import com.tumo.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void buyCreatesOrderAndHoldingWhenHoldingDoesNotExist() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                75000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(stock));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            return order;
        });
        given(holdingRepository.findByUserAndStock(user, stock)).willReturn(Optional.empty());

        OrderResponse response = orderService.buy(1L, new OrderRequest("005930", 10L, OrderType.BUY));

        verify(holdingRepository).save(any(Holding.class));
        assertThat(response.orderId()).isEqualTo(1L);
        assertThat(response.stockCode()).isEqualTo("005930");
        assertThat(response.stockName()).isEqualTo("삼성전자");
        assertThat(response.orderType()).isEqualTo("BUY");
        assertThat(response.quantity()).isEqualTo(10L);
        assertThat(response.executedPrice()).isEqualTo(75000L);
        assertThat(response.totalAmount()).isEqualTo(750000L);
        assertThat(response.cashBalance()).isEqualTo(9250000L);
    }

    @Test
    void buyUpdatesHoldingWhenHoldingExists() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                80000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        Holding holding = new Holding(user, stock, 10L, 70000L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(stock));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            return order;
        });
        given(holdingRepository.findByUserAndStock(user, stock)).willReturn(Optional.of(holding));

        orderService.buy(1L, new OrderRequest("005930", 5L, OrderType.BUY));

        verify(holdingRepository, never()).save(any(Holding.class));
        assertThat(holding.getQuantity()).isEqualTo(15L);
        assertThat(holding.getAveragePrice()).isEqualTo(73333L);
        assertThat(user.getCashBalance()).isEqualTo(9600000L);
    }

    @Test
    void buyThrowsExceptionWhenUserDoesNotExist() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.buy(1L, new OrderRequest("005930", 10L, OrderType.BUY)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND)
                );

        verify(orderRepository, never()).save(any(Order.class));
        verify(holdingRepository, never()).save(any(Holding.class));
    }

    @Test
    void buyThrowsExceptionWhenStockDoesNotExist() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(stockRepository.findByStockCode("999999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.buy(1L, new OrderRequest("999999", 10L, OrderType.BUY)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STOCK_NOT_FOUND)
                );

        verify(orderRepository, never()).save(any(Order.class));
        verify(holdingRepository, never()).save(any(Holding.class));
    }

    @Test
    void buyThrowsExceptionWhenCashBalanceIsInsufficient() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                11000000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(stock));

        assertThatThrownBy(() -> orderService.buy(1L, new OrderRequest("005930", 1L, OrderType.BUY)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_CASH)
                );

        verify(orderRepository, never()).save(any(Order.class));
        verify(holdingRepository, never()).save(any(Holding.class));
    }
}
