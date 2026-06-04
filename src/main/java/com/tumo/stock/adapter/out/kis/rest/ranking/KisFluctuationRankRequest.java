package com.tumo.stock.adapter.out.kis.rest.ranking;

import com.tumo.stock.adapter.out.kis.rest.client.KisRestRequest;
import com.tumo.stock.domain.ranking.StockRankingType;
import com.tumo.stock.domain.stock.Market;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * KIS 국내주식 등락률 순위 조회 요청 값.
 *
 * @param market 랭킹을 조회할 시장
 * @param type 랭킹 기준
 */
record KisFluctuationRankRequest(
        Market market,
        StockRankingType type
) {

    private static final String PATH = "/uapi/domestic-stock/v1/ranking/fluctuation";
    private static final String TRANSACTION_ID = "FHPST01700000";

    KisFluctuationRankRequest {
        if (type != StockRankingType.RISING && type != StockRankingType.FALLING) {
            throw new IllegalArgumentException("등락률 순위 요청 타입이 아닙니다.");
        }
    }

    KisRestRequest<KisStockRankingResponse> toKisRestRequest() {
        return KisRestRequest.get(
                PATH,
                TRANSACTION_ID,
                queryParameters(),
                KisStockRankingResponse.class
        );
    }

    private Map<String, String> queryParameters() {
        Map<String, String> queryParameters = new LinkedHashMap<>();
        queryParameters.put("fid_cond_mrkt_div_code", "J");
        queryParameters.put("fid_cond_scr_div_code", "20170");
        queryParameters.put("fid_input_iscd", marketCode());
        queryParameters.put("fid_rank_sort_cls_code", "0");
        queryParameters.put("fid_input_cnt_1", "0");
        queryParameters.put("fid_prc_cls_code", "0");
        queryParameters.put("fid_input_price_1", "");
        queryParameters.put("fid_input_price_2", "");
        queryParameters.put("fid_vol_cnt", "");
        queryParameters.put("fid_trgt_cls_code", "0");
        queryParameters.put("fid_trgt_exls_cls_code", "0");
        queryParameters.put("fid_div_cls_code", "1");
        queryParameters.put("fid_rsfl_rate1", "");
        queryParameters.put("fid_rsfl_rate2", "");
        return queryParameters;
    }

    private String marketCode() {
        return switch (market) {
            case KOSPI -> "0001";
            case KOSDAQ -> "1001";
        };
    }
}
