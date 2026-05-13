package com.tumo.stock.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "stocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_code", nullable = false, unique = true, length = 20)
    private String stockCode;

    @Column(name = "stock_name", nullable = false, length = 100)
    private String stockName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Market market;

    @Column(name = "current_price", nullable = false)
    private Long currentPrice;

    @Column(name = "price_changed_at", nullable = false)
    private LocalDateTime priceChangedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Stock(String stockCode, String stockName, Market market, Long currentPrice, LocalDateTime priceChangedAt) {
        this.stockCode = stockCode;
        this.stockName = stockName;
        this.market = market;
        this.currentPrice = currentPrice;
        this.priceChangedAt = priceChangedAt;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePrice(Long currentPrice, LocalDateTime priceChangedAt) {
        this.currentPrice = currentPrice;
        this.priceChangedAt = priceChangedAt;
        this.updatedAt = LocalDateTime.now();
    }
}
