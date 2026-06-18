package com.tumo.stock.domain.candle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 특정 종목·시간 단위의 단일 캔들(OHLCV)을 표현하는 JPA 엔티티.
 *
 * <p>동일한 종목 코드, 시간 단위, 캔들 기준 시각의 조합은 유일하다.</p>
 */
@Getter
@Entity
@Table(
        name = "stock_candles",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_stock_candle",
                columnNames = {"stock_code", "candle_interval", "candle_time"}
        ),
        indexes = @Index(
                name = "idx_stock_candle_lookup",
                columnList = "stock_code, candle_interval, candle_time"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockCandle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_code", nullable = false, length = 20)
    private String stockCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "candle_interval", nullable = false, length = 10)
    private CandleInterval interval;

    @Column(name = "candle_time", nullable = false)
    private LocalDateTime candleTime;

    @Column(name = "open_price", nullable = false)
    private Long openPrice;

    @Column(name = "high_price", nullable = false)
    private Long highPrice;

    @Column(name = "low_price", nullable = false)
    private Long lowPrice;

    @Column(name = "close_price", nullable = false)
    private Long closePrice;

    @Column(name = "trade_volume", nullable = false)
    private Long tradeVolume;

    @Column(name = "trade_amount", nullable = false)
    private Long tradeAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public StockCandle(
            String stockCode,
            CandleInterval interval,
            LocalDateTime candleTime,
            Long openPrice,
            Long highPrice,
            Long lowPrice,
            Long closePrice,
            Long tradeVolume,
            Long tradeAmount
    ) {
        if (stockCode == null || stockCode.isBlank()) {
            throw new IllegalArgumentException("종목 코드는 필수입니다.");
        }
        Objects.requireNonNull(interval, "캔들 시간 단위는 필수입니다.");
        Objects.requireNonNull(candleTime, "캔들 기준 시각은 필수입니다.");
        requireNonNegative(openPrice, "시가");
        requireNonNegative(highPrice, "고가");
        requireNonNegative(lowPrice, "저가");
        requireNonNegative(closePrice, "종가");
        requireNonNegative(tradeVolume, "거래량");
        requireNonNegative(tradeAmount, "거래대금");

        this.stockCode = stockCode;
        this.interval = interval;
        this.candleTime = candleTime;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.tradeVolume = tradeVolume;
        this.tradeAmount = tradeAmount;
        this.createdAt = LocalDateTime.now();
    }

    private static void requireNonNegative(Long value, String fieldName) {
        Objects.requireNonNull(value, "%s은(는) 필수입니다.".formatted(fieldName));
        if (value < 0) {
            throw new IllegalArgumentException("%s은(는) 0 이상이어야 합니다.".formatted(fieldName));
        }
    }
}
