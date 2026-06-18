package com.tumo.stock.adapter.out.kis.rest.candle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tumo.stock.domain.candle.CandleInterval;
import com.tumo.stock.domain.candle.StockCandle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * KIS 국내주식기간별시세(일/주/월/년) 조회 응답 값.
 *
 * @param code KIS API 처리 결과 코드
 * @param message KIS API 처리 메시지
 * @param candles KIS 기간별 캔들 목록(output2)
 */
record KisPeriodChartResponse(
        @JsonProperty("rt_cd")
        String code,

        @JsonProperty("msg1")
        String message,

        @JsonProperty("output2")
        List<KisPeriodCandle> candles
) {

    /**
     * KIS 정상 처리 코드.
     */
    private static final String SUCCESS_CODE = "0";

    /**
     * KIS 응답을 Tumo 캔들 목록으로 변환한다. 캔들 기준 시각 오름차순으로 정렬된다.
     *
     * @param requestedStockCode 캔들 조회를 요청한 종목 코드
     * @param interval 캔들 시간 단위
     * @return Tumo 캔들 목록
     */
    List<StockCandle> toStockCandles(String requestedStockCode, CandleInterval interval) {
        Objects.requireNonNull(requestedStockCode, "캔들을 조회한 종목 코드는 필수입니다.");
        Objects.requireNonNull(interval, "캔들 시간 단위는 필수입니다.");

        if (!SUCCESS_CODE.equals(code)) {
            throw new IllegalStateException("KIS 기간별시세 조회에 실패했습니다. code=%s, message=%s".formatted(code, message));
        }

        if (candles == null) {
            return List.of();
        }

        return candles.stream()
                .filter(KisPeriodCandle::hasBusinessDate)
                .map(candle -> candle.toStockCandle(requestedStockCode, interval))
                .sorted((left, right) -> left.getCandleTime().compareTo(right.getCandleTime()))
                .toList();
    }

    /**
     * KIS 기간별시세 output2 단일 캔들.
     *
     * @param businessDate 영업 일자(yyyyMMdd)
     * @param openPrice 시가
     * @param highPrice 고가
     * @param lowPrice 저가
     * @param closePrice 종가
     * @param tradeVolume 거래량
     * @param tradeAmount 거래대금
     */
    record KisPeriodCandle(
            @JsonProperty("stck_bsop_date")
            String businessDate,

            @JsonProperty("stck_oprc")
            String openPrice,

            @JsonProperty("stck_hgpr")
            String highPrice,

            @JsonProperty("stck_lwpr")
            String lowPrice,

            @JsonProperty("stck_clpr")
            String closePrice,

            @JsonProperty("acml_vol")
            String tradeVolume,

            @JsonProperty("acml_tr_pbmn")
            String tradeAmount
    ) {

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

        private boolean hasBusinessDate() {
            return businessDate != null && businessDate.trim().length() == 8;
        }

        private StockCandle toStockCandle(String requestedStockCode, CandleInterval interval) {
            LocalDate date = LocalDate.parse(businessDate.trim(), DATE_FORMATTER);
            return new StockCandle(
                    requestedStockCode,
                    interval,
                    date.atStartOfDay(),
                    KisChartParser.parseLong(openPrice),
                    KisChartParser.parseLong(highPrice),
                    KisChartParser.parseLong(lowPrice),
                    KisChartParser.parseLong(closePrice),
                    KisChartParser.parseLong(tradeVolume),
                    KisChartParser.parseLong(tradeAmount)
            );
        }
    }
}
