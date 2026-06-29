package com.tumo.order.service;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.holding.domain.Holding;
import com.tumo.holding.repository.HoldingRepository;
import com.tumo.order.domain.Order;
import com.tumo.order.dto.OrderHistoryResponse;
import com.tumo.order.dto.OrderPageResponse;
import com.tumo.order.dto.OrderRequest;
import com.tumo.order.dto.OrderResponse;
import com.tumo.order.repository.OrderRepository;
import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.repository.StockRepository;
import com.tumo.user.domain.User;
import com.tumo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final OrderRepository orderRepository;
    private final HoldingRepository holdingRepository;
    private final StockOrderPriceResolver stockOrderPriceResolver;

    /**
     * 주문 유형에 따라 매수/매도로 분기한다.
     */
    @Transactional
    public OrderResponse order(Long userId, OrderRequest request) {
        return switch (request.orderType()) {
            case BUY -> buy(userId, request);
            case SELL -> sell(userId, request);
        };
    }

    @Transactional
    public OrderResponse buy(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Stock stock = stockRepository.findByStockCode(request.stockCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));

        Long executedPrice = stockOrderPriceResolver.resolve(stock);
        Long totalAmount = executedPrice * request.quantity();
        user.decreaseCashBalance(totalAmount);

        Order order = orderRepository.save(
                Order.buy(user, stock, request.quantity(), executedPrice));

        holdingRepository.findByUserAndStock(user, stock)
                .ifPresentOrElse(
                        holding -> holding.buy(request.quantity(), executedPrice),
                        () -> holdingRepository.save(new Holding(user, stock, request.quantity(), executedPrice))
                );

        return OrderResponse.from(order, user.getCashBalance());
    }

    @Transactional
    public OrderResponse sell(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Stock stock = stockRepository.findByStockCode(request.stockCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));
        Holding holding = holdingRepository.findByUserAndStock(user, stock)
                .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_HOLDING));

        Long averagePrice = holding.getAveragePrice();
        Long executedPrice = stockOrderPriceResolver.resolve(stock);
        Long quantity = request.quantity();
        Long realizedProfit = (executedPrice - averagePrice) * quantity;
        Long proceeds = executedPrice * quantity;

        holding.sell(quantity);
        if (holding.getQuantity() == 0) {
            holdingRepository.delete(holding);
        }
        user.increaseCashBalance(proceeds);

        Order order = orderRepository.save(
                Order.sell(user, stock, quantity, executedPrice, realizedProfit));

        return OrderResponse.from(order, user.getCashBalance());
    }

    public OrderPageResponse getOrders(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Page<Order> orderPage = orderRepository.findByUserOrderByExecutedAtDesc(
                user, PageRequest.of(page, size));

        return new OrderPageResponse(
                orderPage.getContent().stream()
                        .map(OrderHistoryResponse::from)
                        .toList(),
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.hasNext()
        );
    }
}
