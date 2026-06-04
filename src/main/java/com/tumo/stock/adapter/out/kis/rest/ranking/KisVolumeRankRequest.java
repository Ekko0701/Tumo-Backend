package com.tumo.stock.adapter.out.kis.rest.ranking;

import com.tumo.stock.adapter.out.kis.rest.client.KisRestRequest;
import com.tumo.stock.domain.ranking.StockRankingType;
import com.tumo.stock.domain.stock.Market;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * KIS 국내주식 거래량/거래대금 순위 조회 요청 값.
 *
 * @param market 랭킹을 조회할 시장
 * @param type 랭킹 기준
 */
record KisVolumeRankRequest(
        Market market,
        StockRankingType type
) {

    private static final String PATH = "/uapi/domestic-stock/v1/quotations/volume-rank";
    private static final String TRANSACTION_ID = "FHPST01710000";

    KisVolumeRankRequest {
        if (type != StockRankingType.TRADE_AMOUNT && type != StockRankingType.TRADE_VOLUME) {
            throw new IllegalArgumentException("거래량/거래대금 순위 요청 타입이 아닙니다.");
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
        queryParameters.put("FID_COND_MRKT_DIV_CODE", "J");
        queryParameters.put("FID_COND_SCR_DIV_CODE", "20171");
        queryParameters.put("FID_INPUT_ISCD", marketCode());
        queryParameters.put("FID_DIV_CLS_CODE", "1");
        queryParameters.put("FID_BLNG_CLS_CODE", rankingSortCode());
        queryParameters.put("FID_TRGT_CLS_CODE", "111111111");
        queryParameters.put("FID_TRGT_EXLS_CLS_CODE", "0000000000");
        queryParameters.put("FID_INPUT_PRICE_1", "0");
        queryParameters.put("FID_INPUT_PRICE_2", "0");
        queryParameters.put("FID_VOL_CNT", "0");
        queryParameters.put("FID_INPUT_DATE_1", "0");
        return queryParameters;
    }

    private String marketCode() {
        return switch (market) {
            case KOSPI -> "0001";
            case KOSDAQ -> "1001";
        };
    }

    private String rankingSortCode() {
        return switch (type) {
            case TRADE_VOLUME -> "0";
            case TRADE_AMOUNT -> "3";
            case RISING, FALLING, POPULAR -> throw new IllegalArgumentException("거래량/거래대금 순위 요청 타입이 아닙니다.");
        };
    }
}
