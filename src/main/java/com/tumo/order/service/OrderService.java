package com.tumo.order.service;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.holding.domain.Holding;
import com.tumo.holding.repository.HoldingRepository;
import com.tumo.order.domain.Order;
import com.tumo.order.dto.OrderRequest;
import com.tumo.order.dto.OrderResponse;
import com.tumo.order.repository.OrderRepository;
import com.tumo.stock.domain.Stock;
import com.tumo.stock.repository.StockRepository;
import com.tumo.user.domain.User;
import com.tumo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    @Transactional
    public OrderResponse buy(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Stock stock = stockRepository.findByStockCode(request.stockCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));

        Long executedPrice = stock.getCurrentPrice();
        Long totalAmount = executedPrice * request.quantity();
        user.decreaseCashBalance(totalAmount);

        Order order = orderRepository.save(new Order(
                user,
                stock,
                request.orderType(),
                request.quantity(),
                executedPrice
        ));

        holdingRepository.findByUserAndStock(user, stock)
                .ifPresentOrElse(
                        holding -> holding.buy(request.quantity(), executedPrice),
                        () -> holdingRepository.save(new Holding(user, stock, request.quantity(), executedPrice))
                );

        return OrderResponse.from(order, user.getCashBalance());
    }
}
