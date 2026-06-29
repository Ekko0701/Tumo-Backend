package com.tumo.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.holding.domain.Holding;
import com.tumo.holding.repository.HoldingRepository;
import com.tumo.order.domain.Order;
import com.tumo.order.domain.OrderType;
import com.tumo.order.dto.OrderPageResponse;
import com.tumo.order.dto.OrderRequest;
import com.tumo.order.dto.OrderResponse;
import com.tumo.order.repository.OrderRepository;
import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.repository.StockRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Mock
    private StockOrderPriceResolver stockOrderPriceResolver;

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
        given(stockOrderPriceResolver.resolve(stock)).willReturn(76000L);

        OrderResponse response = orderService.buy(1L, new OrderRequest("005930", 10L, OrderType.BUY));

        verify(holdingRepository).save(any(Holding.class));
        assertThat(response.orderId()).isEqualTo(1L);
        assertThat(response.stockCode()).isEqualTo("005930");
        assertThat(response.stockName()).isEqualTo("삼성전자");
        assertThat(response.orderType()).isEqualTo("BUY");
        assertThat(response.quantity()).isEqualTo(10L);
        assertThat(response.executedPrice()).isEqualTo(76000L);
        assertThat(response.totalAmount()).isEqualTo(760000L);
        assertThat(response.cashBalance()).isEqualTo(9240000L);
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
        given(stockOrderPriceResolver.resolve(stock)).willReturn(80000L);

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
        given(stockOrderPriceResolver.resolve(stock)).willReturn(11000000L);

        assertThatThrownBy(() -> orderService.buy(1L, new OrderRequest("005930", 1L, OrderType.BUY)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_CASH)
                );

        verify(orderRepository, never()).save(any(Order.class));
        verify(holdingRepository, never()).save(any(Holding.class));
    }

    @Test
    void buyThrowsExceptionWhenOrderPriceIsUnavailable() {
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
        given(stockOrderPriceResolver.resolve(stock))
                .willThrow(new BusinessException(ErrorCode.STOCK_PRICE_UNAVAILABLE));

        assertThatThrownBy(() -> orderService.buy(1L, new OrderRequest("005930", 10L, OrderType.BUY)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STOCK_PRICE_UNAVAILABLE)
                );

        verify(orderRepository, never()).save(any(Order.class));
        verify(holdingRepository, never()).save(any(Holding.class));
        assertThat(user.getCashBalance()).isEqualTo(10_000_000L);
    }

    @Test
    void sellReducesHoldingQuantityAndIncreasesCash() {
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
        given(holdingRepository.findByUserAndStock(user, stock)).willReturn(Optional.of(holding));
        given(stockOrderPriceResolver.resolve(stock)).willReturn(80000L);
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            return order;
        });

        OrderResponse response = orderService.sell(1L, new OrderRequest("005930", 4L, OrderType.SELL));

        verify(holdingRepository, never()).delete(any(Holding.class));
        assertThat(holding.getQuantity()).isEqualTo(6L);
        assertThat(holding.getAveragePrice()).isEqualTo(70000L);
        assertThat(response.orderType()).isEqualTo("SELL");
        assertThat(response.quantity()).isEqualTo(4L);
        assertThat(response.executedPrice()).isEqualTo(80000L);
        assertThat(response.totalAmount()).isEqualTo(320000L);
        assertThat(response.realizedProfit()).isEqualTo(40000L);
        assertThat(response.cashBalance()).isEqualTo(10_320_000L);
        assertThat(user.getCashBalance()).isEqualTo(10_320_000L);
    }

    @Test
    void sellDeletesHoldingWhenAllSharesSold() {
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
        given(holdingRepository.findByUserAndStock(user, stock)).willReturn(Optional.of(holding));
        given(stockOrderPriceResolver.resolve(stock)).willReturn(80000L);
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            return order;
        });

        OrderResponse response = orderService.sell(1L, new OrderRequest("005930", 10L, OrderType.SELL));

        verify(holdingRepository).delete(holding);
        assertThat(holding.getQuantity()).isEqualTo(0L);
        assertThat(response.realizedProfit()).isEqualTo(100000L);
        assertThat(user.getCashBalance()).isEqualTo(10_800_000L);
    }

    @Test
    void sellRecordsNegativeRealizedProfitOnLoss() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                60000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        Holding holding = new Holding(user, stock, 10L, 70000L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(stock));
        given(holdingRepository.findByUserAndStock(user, stock)).willReturn(Optional.of(holding));
        given(stockOrderPriceResolver.resolve(stock)).willReturn(60000L);
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            return order;
        });

        OrderResponse response = orderService.sell(1L, new OrderRequest("005930", 5L, OrderType.SELL));

        assertThat(response.realizedProfit()).isEqualTo(-50000L);
        assertThat(holding.getQuantity()).isEqualTo(5L);
        assertThat(user.getCashBalance()).isEqualTo(10_300_000L);
    }

    @Test
    void sellThrowsExceptionWhenHoldingDoesNotExist() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                80000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(stock));
        given(holdingRepository.findByUserAndStock(user, stock)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.sell(1L, new OrderRequest("005930", 1L, OrderType.SELL)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_HOLDING)
                );

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void sellThrowsExceptionWhenHoldingQuantityIsInsufficient() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                80000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        Holding holding = new Holding(user, stock, 3L, 70000L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(stock));
        given(holdingRepository.findByUserAndStock(user, stock)).willReturn(Optional.of(holding));
        given(stockOrderPriceResolver.resolve(stock)).willReturn(80000L);

        assertThatThrownBy(() -> orderService.sell(1L, new OrderRequest("005930", 5L, OrderType.SELL)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_HOLDING)
                );

        verify(orderRepository, never()).save(any(Order.class));
        verify(holdingRepository, never()).delete(any(Holding.class));
        assertThat(holding.getQuantity()).isEqualTo(3L);
        assertThat(user.getCashBalance()).isEqualTo(10_000_000L);
    }

    @Test
    void sellThrowsExceptionWhenOrderPriceIsUnavailable() {
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
        given(holdingRepository.findByUserAndStock(user, stock)).willReturn(Optional.of(holding));
        given(stockOrderPriceResolver.resolve(stock))
                .willThrow(new BusinessException(ErrorCode.STOCK_PRICE_UNAVAILABLE));

        assertThatThrownBy(() -> orderService.sell(1L, new OrderRequest("005930", 5L, OrderType.SELL)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STOCK_PRICE_UNAVAILABLE)
                );

        verify(orderRepository, never()).save(any(Order.class));
        assertThat(holding.getQuantity()).isEqualTo(10L);
        assertThat(user.getCashBalance()).isEqualTo(10_000_000L);
    }

    @Test
    void orderDelegatesToBuyWhenOrderTypeIsBuy() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                76000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(stock));
        given(holdingRepository.findByUserAndStock(user, stock)).willReturn(Optional.empty());
        given(stockOrderPriceResolver.resolve(stock)).willReturn(76000L);
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            return order;
        });

        OrderResponse response = orderService.order(1L, new OrderRequest("005930", 10L, OrderType.BUY));

        verify(holdingRepository).save(any(Holding.class));
        assertThat(response.orderType()).isEqualTo("BUY");
        assertThat(response.realizedProfit()).isNull();
        assertThat(user.getCashBalance()).isEqualTo(9_240_000L);
    }

    @Test
    void orderDelegatesToSellWhenOrderTypeIsSell() {
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
        given(holdingRepository.findByUserAndStock(user, stock)).willReturn(Optional.of(holding));
        given(stockOrderPriceResolver.resolve(stock)).willReturn(80000L);
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 1L);
            return order;
        });

        OrderResponse response = orderService.order(1L, new OrderRequest("005930", 4L, OrderType.SELL));

        assertThat(response.orderType()).isEqualTo("SELL");
        assertThat(response.realizedProfit()).isEqualTo(40000L);
        assertThat(holding.getQuantity()).isEqualTo(6L);
    }

    @Test
    void getOrdersReturnsPagedOrderHistory() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                75000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        Order order = Order.buy(user, stock, 10L, 75000L);
        ReflectionTestUtils.setField(order, "id", 1L);
        Page<Order> orderPage = new PageImpl<>(List.of(order), PageRequest.of(0, 30), 1);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(orderRepository.findByUserOrderByExecutedAtDesc(eq(user), any(Pageable.class)))
                .willReturn(orderPage);

        OrderPageResponse response = orderService.getOrders(1L, 0, 30);

        assertThat(response.orders()).hasSize(1);
        assertThat(response.orders().get(0).stockCode()).isEqualTo("005930");
        assertThat(response.orders().get(0).orderType()).isEqualTo("BUY");
        assertThat(response.orders().get(0).executedPrice()).isEqualTo(75000L);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(30);
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    void getOrdersClampsOversizedPageSize() {
        User user = new User("test@example.com", "encoded-password", "tester");
        ReflectionTestUtils.setField(user, "id", 1L);
        Stock stock = new Stock(
                "005930",
                "삼성전자",
                Market.KOSPI,
                75000L,
                LocalDateTime.of(2026, 5, 13, 15, 30)
        );
        Order order = Order.buy(user, stock, 1L, 75000L);
        ReflectionTestUtils.setField(order, "id", 1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(orderRepository.findByUserOrderByExecutedAtDesc(eq(user), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(order), PageRequest.of(0, 100), 1));

        orderService.getOrders(1L, 0, 1000);

        verify(orderRepository).findByUserOrderByExecutedAtDesc(user, PageRequest.of(0, 100));
    }
}
