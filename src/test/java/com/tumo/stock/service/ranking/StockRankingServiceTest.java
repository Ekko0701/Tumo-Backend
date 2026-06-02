package com.tumo.stock.service.ranking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.domain.ranking.StockRankingType;
import com.tumo.stock.domain.stock.Market;
import org.junit.jupiter.api.Test;

class StockRankingServiceTest {

    private final StockRankingService stockRankingService = new StockRankingService();

    @Test
    void getRankingsThrowsExceptionWhenRankingTypeIsNotSupportedYet() {
        assertThatThrownBy(() -> stockRankingService.getRankings(
                Market.KOSPI,
                StockRankingType.TRADE_AMOUNT,
                0,
                30
        ))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STOCK_RANKING_NOT_SUPPORTED)
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
}
