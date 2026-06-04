package com.tumo.stock.adapter.out.kis.rest.ranking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tumo.stock.domain.ranking.StockRanking;
import com.tumo.stock.domain.stock.Market;
import java.util.List;
import org.junit.jupiter.api.Test;

class KisStockRankingResponseTest {

    @Test
    void toStockRankings() {
        KisStockRankingResponse response = new KisStockRankingResponse(
                "0",
                "MCA00000",
                "정상처리 되었습니다.",
                List.of(new KisStockRankingResponse.KisStockRankingOutput(
                        "005930",
                        "삼성전자",
                        "80000",
                        "500",
                        "0.63",
                        "1000000",
                        "80000000000"
                ))
        );

        List<StockRanking> stockRankings = response.toStockRankings(Market.KOSPI);

        assertThat(stockRankings).hasSize(1);
        StockRanking stockRanking = stockRankings.getFirst();
        assertThat(stockRanking.stockCode()).isEqualTo("005930");
        assertThat(stockRanking.stockName()).isEqualTo("삼성전자");
        assertThat(stockRanking.market()).isEqualTo(Market.KOSPI);
        assertThat(stockRanking.currentPrice()).isEqualTo(80000L);
        assertThat(stockRanking.changePrice()).isEqualTo(500L);
        assertThat(stockRanking.changeRate()).isEqualByComparingTo("0.63");
        assertThat(stockRanking.tradeVolume()).isEqualTo(1000000L);
        assertThat(stockRanking.tradeAmount()).isEqualTo(80000000000L);
        assertThat(stockRanking.rankedAt()).isNotNull();
    }

    @Test
    void throwsExceptionWhenKisResponseIsFailure() {
        KisStockRankingResponse response = new KisStockRankingResponse(
                "1",
                "EGW00123",
                "오류가 발생했습니다.",
                null
        );

        assertThatThrownBy(() -> response.toStockRankings(Market.KOSPI))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("KIS 종목 랭킹 조회에 실패했습니다. code=1, message=오류가 발생했습니다.");
    }
}
