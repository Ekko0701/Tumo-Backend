package com.tumo.order.domain;

import com.tumo.stock.domain.stock.Stock;
import com.tumo.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;

    @Column(nullable = false)
    private Long quantity;

    @Column(name = "executed_price", nullable = false)
    private Long executedPrice;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "realized_profit")
    private Long realizedProfit;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private Order(
            User user,
            Stock stock,
            OrderType orderType,
            Long quantity,
            Long executedPrice,
            Long realizedProfit
    ) {
        this.user = user;
        this.stock = stock;
        this.orderType = orderType;
        this.quantity = quantity;
        this.executedPrice = executedPrice;
        this.totalAmount = executedPrice * quantity;
        this.realizedProfit = realizedProfit;
        this.executedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 매수 주문을 생성한다. 실현손익은 없다(null).
     */
    public static Order buy(User user, Stock stock, Long quantity, Long executedPrice) {
        return new Order(user, stock, OrderType.BUY, quantity, executedPrice, null);
    }

    /**
     * 매도 주문을 생성한다. 실현손익 = (체결가 - 평균매입가) × 수량.
     */
    public static Order sell(
            User user,
            Stock stock,
            Long quantity,
            Long executedPrice,
            Long realizedProfit
    ) {
        return new Order(user, stock, OrderType.SELL, quantity, executedPrice, realizedProfit);
    }
}
