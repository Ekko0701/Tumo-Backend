package com.tumo.stock.adapter.out.kis.rest.ranking;

import static org.assertj.core.api.Assertions.assertThat;

import com.tumo.stock.adapter.out.kis.rest.client.KisRestRequest;
import com.tumo.stock.domain.ranking.StockRankingType;
import com.tumo.stock.domain.stock.Market;
import org.junit.jupiter.api.Test;

class KisFluctuationRankRequestTest {

    @Test
    void toKisRestRequest() {
        KisFluctuationRankRequest request = new KisFluctuationRankRequest(Market.KOSDAQ, StockRankingType.RISING);

        KisRestRequest<KisStockRankingResponse> kisRestRequest = request.toKisRestRequest();

        assertThat(kisRestRequest.path()).isEqualTo("/uapi/domestic-stock/v1/ranking/fluctuation");
        assertThat(kisRestRequest.transactionId()).isEqualTo("FHPST01700000");
        assertThat(kisRestRequest.queryParameters())
                .containsEntry("fid_cond_scr_div_code", "20170")
                .containsEntry("fid_input_iscd", "1001")
                .containsEntry("fid_rank_sort_cls_code", "0")
                .containsEntry("fid_div_cls_code", "1");
    }
}
