package com.tumo.stock.adapter.out.kis.rest.ranking;

import static org.assertj.core.api.Assertions.assertThat;

import com.tumo.stock.adapter.out.kis.rest.client.KisRestRequest;
import com.tumo.stock.domain.ranking.StockRankingType;
import com.tumo.stock.domain.stock.Market;
import org.junit.jupiter.api.Test;

class KisVolumeRankRequestTest {

    @Test
    void toKisRestRequestForTradeAmount() {
        KisVolumeRankRequest request = new KisVolumeRankRequest(Market.KOSPI, StockRankingType.TRADE_AMOUNT);

        KisRestRequest<KisStockRankingResponse> kisRestRequest = request.toKisRestRequest();

        assertThat(kisRestRequest.path()).isEqualTo("/uapi/domestic-stock/v1/quotations/volume-rank");
        assertThat(kisRestRequest.transactionId()).isEqualTo("FHPST01710000");
        assertThat(kisRestRequest.queryParameters())
                .containsEntry("FID_INPUT_ISCD", "0001")
                .containsEntry("FID_DIV_CLS_CODE", "1")
                .containsEntry("FID_BLNG_CLS_CODE", "3");
    }

    @Test
    void toKisRestRequestForTradeVolume() {
        KisVolumeRankRequest request = new KisVolumeRankRequest(Market.KOSDAQ, StockRankingType.TRADE_VOLUME);

        KisRestRequest<KisStockRankingResponse> kisRestRequest = request.toKisRestRequest();

        assertThat(kisRestRequest.queryParameters())
                .containsEntry("FID_INPUT_ISCD", "1001")
                .containsEntry("FID_BLNG_CLS_CODE", "0");
    }
}
