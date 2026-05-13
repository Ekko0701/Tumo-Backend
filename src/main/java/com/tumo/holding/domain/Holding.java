package com.tumo.holding.domain;

import com.tumo.stock.domain.Stock;
import com.tumo.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "holdings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_holdings_user_stock",
                columnNames = {"user_id", "stock_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private Long quantity;

    @Column(name = "average_price", nullable = false)
    private Long averagePrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Holding(User user, Stock stock, Long quantity, Long averagePrice) {
        this.user = user;
        this.stock = stock;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 추가 매수 수량과 체결가를 현재 보유 상태에 반영하고 평균 매입가를 재계산한다.
     */
    public void buy(Long quantity, Long price) {
        long totalQuantity = this.quantity + quantity;
        long totalAmount = this.averagePrice * this.quantity + price * quantity;

        this.quantity = totalQuantity;
        this.averagePrice = totalAmount / totalQuantity;
        this.updatedAt = LocalDateTime.now();
    }
}
