package com.tumo.stock.adapter.out.kis.rest.quotation;

import com.tumo.stock.adapter.out.kis.rest.client.KisRestRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * KIS 국내주식 현재가 조회 요청 값을 표현하는 값 객체.
 *
 * @param stockCode 현재가를 조회할 종목 코드
 */
record KisInquirePriceRequest(String stockCode) {

    /**
     * KIS 국내주식 현재가 조회 API path.
     */
    private static final String PATH = "/uapi/domestic-stock/v1/quotations/inquire-price";

    /**
     * KIS 국내주식 현재가 조회 transaction id.
     */
    private static final String TRANSACTION_ID = "FHKST01010100";

    /**
     * KIS 국내주식 시장 구분 코드 query parameter 이름.
     */
    private static final String MARKET_DIV_CODE_PARAMETER = "FID_COND_MRKT_DIV_CODE";

    /**
     * KIS 종목 코드 query parameter 이름.
     */
    private static final String STOCK_CODE_PARAMETER = "FID_INPUT_ISCD";

    /**
     * KIS 국내주식 시장 구분 코드.
     */
    private static final String DOMESTIC_STOCK_MARKET_DIV_CODE = "J";

    /**
     * KIS 국내주식 현재가 조회 요청 값을 생성한다.
     *
     * @param stockCode 현재가를 조회할 종목 코드
     */
    KisInquirePriceRequest {
        if (stockCode == null || stockCode.isBlank()) {
            throw new IllegalArgumentException("현재가를 조회할 종목 코드는 필수입니다.");
        }
    }

    /**
     * KIS 공통 REST 요청 값으로 변환한다.
     *
     * @return KIS 공통 REST 요청 값
     */
    KisRestRequest<KisInquirePriceResponse> toKisRestRequest() {
        return KisRestRequest.get(
                PATH,
                TRANSACTION_ID,
                queryParameters(),
                KisInquirePriceResponse.class
        );
    }

    private Map<String, String> queryParameters() {
        Map<String, String> queryParameters = new LinkedHashMap<>();
        queryParameters.put(MARKET_DIV_CODE_PARAMETER, DOMESTIC_STOCK_MARKET_DIV_CODE);
        queryParameters.put(STOCK_CODE_PARAMETER, stockCode);
        return queryParameters;
    }
}
