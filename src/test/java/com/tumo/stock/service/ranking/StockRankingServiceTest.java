package com.tumo.stock.service.ranking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.domain.ranking.StockRanking;
import com.tumo.stock.domain.ranking.StockRankingType;
import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.dto.StockPageResponse;
import com.tumo.stock.port.query.StockRankingQueryPort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class StockRankingServiceTest {

    private StockRankingQueryPort stockRankingQueryPort;
    private StockRankingService stockRankingService;

    @BeforeEach
    void setUp() {
        stockRankingQueryPort = Mockito.mock(StockRankingQueryPort.class);
        stockRankingService = new StockRankingService(stockRankingQueryPort);
    }

    @Test
    void getRankings() {
        LocalDateTime rankedAt = LocalDateTime.of(2026, 6, 4, 9, 30);
        given(stockRankingQueryPort.findRankings(Market.KOSPI, StockRankingType.TRADE_AMOUNT))
                .willReturn(List.of(
                        stockRanking("005930", "삼성전자", 80000L, 500L, "0.63", 1000000L, 80000000000L, rankedAt),
                        stockRanking("000660", "SK하이닉스", 190000L, 1000L, "0.53", 500000L, 95000000000L, rankedAt),
                        stockRanking("035420", "NAVER", 210000L, 2000L, "0.96", 300000L, 63000000000L, rankedAt)
                ));

        StockPageResponse response = stockRankingService.getRankings(Market.KOSPI, StockRankingType.TRADE_AMOUNT, 0, 2);

        assertThat(response.stocks()).hasSize(2);
        assertThat(response.stocks())
                .extracting(stock -> stock.stockCode())
                .containsExactly("005930", "000660");
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    void getRankingsReturnsEmptyPageWhenPageIsOutOfRange() {
        given(stockRankingQueryPort.findRankings(Market.KOSPI, StockRankingType.TRADE_VOLUME))
                .willReturn(List.of());

        StockPageResponse response = stockRankingService.getRankings(Market.KOSPI, StockRankingType.TRADE_VOLUME, 1, 30);

        assertThat(response.stocks()).isEmpty();
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(30);
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    void getRankingsThrowsExceptionWhenPageRequestIsInvalid() {
        assertThatThrownBy(() -> stockRankingService.getRankings(
                Market.KOSPI,
                StockRankingType.TRADE_AMOUNT,
                -1,
                30
        ))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST)
                );
    }

    @Test
    void getRankingsThrowsExceptionWhenPopularRankingIsRequested() {
        assertThatThrownBy(() -> stockRankingService.getRankings(
                Market.KOSPI,
                StockRankingType.POPULAR,
                0,
                30
        ))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STOCK_RANKING_NOT_SUPPORTED)
                );
    }

    private StockRanking stockRanking(
            String stockCode,
            String stockName,
            Long currentPrice,
            Long changePrice,
            String changeRate,
            Long tradeVolume,
            Long tradeAmount,
            LocalDateTime rankedAt
    ) {
        return new StockRanking(
                stockCode,
                stockName,
                Market.KOSPI,
                currentPrice,
                changePrice,
                new BigDecimal(changeRate),
                tradeVolume,
                tradeAmount,
                rankedAt
        );
    }
}
