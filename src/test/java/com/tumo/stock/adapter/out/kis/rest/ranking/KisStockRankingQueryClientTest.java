package com.tumo.stock.adapter.out.kis.rest.ranking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tumo.stock.adapter.out.kis.rest.client.KisRestClient;
import com.tumo.stock.adapter.out.kis.rest.client.KisRestRequest;
import com.tumo.stock.domain.ranking.StockRanking;
import com.tumo.stock.domain.ranking.StockRankingType;
import com.tumo.stock.domain.stock.Market;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KisStockRankingQueryClientTest {

    private final KisRestClient restClient = org.mockito.Mockito.mock(KisRestClient.class);
    private final KisStockRankingQueryClient client = new KisStockRankingQueryClient(restClient);

    @Test
    void findTradeAmountRankings() {
        given(restClient.get(any())).willReturn(kisResponse("005930", "삼성전자", "0.63"));

        List<StockRanking> rankings = client.findRankings(Market.KOSPI, StockRankingType.TRADE_AMOUNT);

        assertThat(rankings).hasSize(1);
        assertThat(rankings.getFirst().stockCode()).isEqualTo("005930");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<KisRestRequest<KisStockRankingResponse>> requestCaptor =
                ArgumentCaptor.forClass(KisRestRequest.class);
        verify(restClient).get(requestCaptor.capture());

        KisRestRequest<KisStockRankingResponse> request = requestCaptor.getValue();
        assertThat(request.path()).isEqualTo("/uapi/domestic-stock/v1/quotations/volume-rank");
        assertThat(request.queryParameters())
                .containsEntry("FID_INPUT_ISCD", "0001")
                .containsEntry("FID_BLNG_CLS_CODE", "3");
    }

    @Test
    void findRisingRankingsSortsByChangeRateDescending() {
        given(restClient.get(any())).willReturn(new KisStockRankingResponse(
                "0",
                "MCA00000",
                "정상처리 되었습니다.",
                List.of(
                        kisOutput("005930", "삼성전자", "0.63"),
                        kisOutput("000660", "SK하이닉스", "2.15")
                )
        ));

        List<StockRanking> rankings = client.findRankings(Market.KOSPI, StockRankingType.RISING);

        assertThat(rankings)
                .extracting(StockRanking::stockCode)
                .containsExactly("000660", "005930");
    }

    @Test
    void findFallingRankingsSortsByChangeRateAscending() {
        given(restClient.get(any())).willReturn(new KisStockRankingResponse(
                "0",
                "MCA00000",
                "정상처리 되었습니다.",
                List.of(
                        kisOutput("005930", "삼성전자", "-0.63"),
                        kisOutput("000660", "SK하이닉스", "-2.15")
                )
        ));

        List<StockRanking> rankings = client.findRankings(Market.KOSPI, StockRankingType.FALLING);

        assertThat(rankings)
                .extracting(StockRanking::stockCode)
                .containsExactly("000660", "005930");
    }

    private KisStockRankingResponse kisResponse(String stockCode, String stockName, String changeRate) {
        return new KisStockRankingResponse(
                "0",
                "MCA00000",
                "정상처리 되었습니다.",
                List.of(kisOutput(stockCode, stockName, changeRate))
        );
    }

    private KisStockRankingResponse.KisStockRankingOutput kisOutput(
            String stockCode,
            String stockName,
            String changeRate
    ) {
        return new KisStockRankingResponse.KisStockRankingOutput(
                stockCode,
                stockName,
                "80000",
                "500",
                changeRate,
                "1000000",
                "80000000000"
        );
    }
}
