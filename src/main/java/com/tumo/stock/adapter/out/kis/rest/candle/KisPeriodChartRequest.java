package com.tumo.stock.adapter.out.kis.rest.candle;

import com.tumo.stock.adapter.out.kis.rest.client.KisRestRequest;
import com.tumo.stock.domain.candle.CandleInterval;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * KIS 국내주식기간별시세(일/주/월/년) 조회 요청 값을 표현하는 값 객체.
 *
 * @param stockCode 캔들을 조회할 종목 코드
 * @param interval 캔들 시간 단위(일/주/월/년)
 * @param from 조회 시작 일자(포함)
 * @param to 조회 종료 일자(포함)
 */
record KisPeriodChartRequest(String stockCode, CandleInterval interval, LocalDate from, LocalDate to) {

    /**
     * KIS 국내주식기간별시세 조회 API path.
     */
    private static final String PATH = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";

    /**
     * KIS 국내주식기간별시세 조회 transaction id.
     */
    private static final String TRANSACTION_ID = "FHKST03010100";

    /**
     * KIS 1회 응답 최대 캔들 수.
     */
    static final int MAX_RECORDS_PER_CALL = 100;

    private static final String MARKET_DIV_CODE_PARAMETER = "FID_COND_MRKT_DIV_CODE";
    private static final String STOCK_CODE_PARAMETER = "FID_INPUT_ISCD";
    private static final String START_DATE_PARAMETER = "FID_INPUT_DATE_1";
    private static final String END_DATE_PARAMETER = "FID_INPUT_DATE_2";
    private static final String PERIOD_DIV_CODE_PARAMETER = "FID_PERIOD_DIV_CODE";
    private static final String ADJUST_PRICE_PARAMETER = "FID_ORG_ADJ_PRC";

    private static final String DOMESTIC_STOCK_MARKET_DIV_CODE = "J";

    /**
     * 수정주가 사용 코드. 액면분할·배당으로 인한 차트 단절을 막기 위해 수정주가를 사용한다.
     */
    private static final String ADJUSTED_PRICE_CODE = "0";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    KisPeriodChartRequest {
        if (stockCode == null || stockCode.isBlank()) {
            throw new IllegalArgumentException("캔들을 조회할 종목 코드는 필수입니다.");
        }
        Objects.requireNonNull(interval, "캔들 시간 단위는 필수입니다.");
        if (interval.isMinute()) {
            throw new IllegalArgumentException("분봉은 기간별시세 조회 대상이 아닙니다.");
        }
        Objects.requireNonNull(from, "조회 시작 일자는 필수입니다.");
        Objects.requireNonNull(to, "조회 종료 일자는 필수입니다.");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("조회 시작 일자는 종료 일자보다 이후일 수 없습니다.");
        }
    }

    /**
     * KIS 공통 REST 요청 값으로 변환한다.
     *
     * @return KIS 공통 REST 요청 값
     */
    KisRestRequest<KisPeriodChartResponse> toKisRestRequest() {
        return KisRestRequest.get(
                PATH,
                TRANSACTION_ID,
                queryParameters(),
                KisPeriodChartResponse.class
        );
    }

    private Map<String, String> queryParameters() {
        Map<String, String> queryParameters = new LinkedHashMap<>();
        queryParameters.put(MARKET_DIV_CODE_PARAMETER, DOMESTIC_STOCK_MARKET_DIV_CODE);
        queryParameters.put(STOCK_CODE_PARAMETER, stockCode);
        queryParameters.put(START_DATE_PARAMETER, from.format(DATE_FORMATTER));
        queryParameters.put(END_DATE_PARAMETER, to.format(DATE_FORMATTER));
        queryParameters.put(PERIOD_DIV_CODE_PARAMETER, interval.kisPeriodCode());
        queryParameters.put(ADJUST_PRICE_PARAMETER, ADJUSTED_PRICE_CODE);
        return queryParameters;
    }
}
