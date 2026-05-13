package com.tumo.order.domain;

import com.tumo.stock.domain.Stock;
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

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Order(User user, Stock stock, OrderType orderType, Long quantity, Long executedPrice) {
        this.user = user;
        this.stock = stock;
        this.orderType = orderType;
        this.quantity = quantity;
        this.executedPrice = executedPrice;
        this.totalAmount = executedPrice * quantity;
        this.executedAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
}
