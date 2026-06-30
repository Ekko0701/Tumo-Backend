package com.tumo.stock.adapter.out.kis.rest.candle;

import com.tumo.stock.adapter.out.kis.rest.client.KisRestRequest;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * KIS 주식당일분봉조회 요청 값을 표현하는 값 객체.
 *
 * @param stockCode 분봉을 조회할 종목 코드
 * @param hourCursor 조회 기준 시각(이 시각 이전 분봉을 최대 30건 반환)
 */
record KisTodayMinuteChartRequest(String stockCode, LocalTime hourCursor) {

    private static final String PATH = "/uapi/domestic-stock/v1/quotations/inquire-time-itemchartprice";
    private static final String TRANSACTION_ID = "FHKST03010200";

    /**
     * KIS 1회 응답 최대 분봉 수.
     */
    static final int MAX_RECORDS_PER_CALL = 30;

    private static final String ETC_CLS_CODE_PARAMETER = "FID_ETC_CLS_CODE";
    private static final String MARKET_DIV_CODE_PARAMETER = "FID_COND_MRKT_DIV_CODE";
    private static final String STOCK_CODE_PARAMETER = "FID_INPUT_ISCD";
    private static final String HOUR_PARAMETER = "FID_INPUT_HOUR_1";
    private static final String PAST_DATA_INCLUDE_PARAMETER = "FID_PW_DATA_INCU_YN";

    private static final String DOMESTIC_STOCK_MARKET_DIV_CODE = "J";
    private static final String INCLUDE = "Y";
    private static final String EMPTY = "";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    KisTodayMinuteChartRequest {
        if (stockCode == null || stockCode.isBlank()) {
            throw new IllegalArgumentException("분봉을 조회할 종목 코드는 필수입니다.");
        }
        Objects.requireNonNull(hourCursor, "분봉 조회 기준 시각은 필수입니다.");
    }

    KisRestRequest<KisMinuteChartResponse> toKisRestRequest() {
        return KisRestRequest.get(
                PATH,
                TRANSACTION_ID,
                queryParameters(),
                KisMinuteChartResponse.class
        );
    }

    private Map<String, String> queryParameters() {
        Map<String, String> queryParameters = new LinkedHashMap<>();
        queryParameters.put(ETC_CLS_CODE_PARAMETER, EMPTY);
        queryParameters.put(MARKET_DIV_CODE_PARAMETER, DOMESTIC_STOCK_MARKET_DIV_CODE);
        queryParameters.put(STOCK_CODE_PARAMETER, stockCode);
        queryParameters.put(HOUR_PARAMETER, hourCursor.format(TIME_FORMATTER));
        queryParameters.put(PAST_DATA_INCLUDE_PARAMETER, INCLUDE);
        return queryParameters;
    }
}
