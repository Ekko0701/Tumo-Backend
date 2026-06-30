package com.tumo.stock.adapter.out.kis.rest.candle;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tumo.stock.domain.candle.CandleInterval;
import com.tumo.stock.domain.candle.StockCandle;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * KIS 분봉(당일/일별) 조회 응답 값. 당일분봉·일별분봉 API가 동일한 output2 구조를 사용한다.
 *
 * @param code KIS API 처리 결과 코드
 * @param message KIS API 처리 메시지
 * @param candles KIS 분봉 캔들 목록(output2)
 */
record KisMinuteChartResponse(
        @JsonProperty("rt_cd")
        String code,

        @JsonProperty("msg1")
        String message,

        @JsonProperty("output2")
        List<KisMinuteCandle> candles
) {

    /**
     * KIS 정상 처리 코드.
     */
    private static final String SUCCESS_CODE = "0";

    /**
     * KIS 분봉 응답을 Tumo 캔들 목록으로 변환한다. 캔들 기준 시각 오름차순으로 정렬된다.
     *
     * @param requestedStockCode 캔들 조회를 요청한 종목 코드
     * @param fallbackDate output2 행에 영업 일자가 없을 때 사용할 기준 일자
     * @return Tumo 캔들 목록
     */
    List<StockCandle> toStockCandles(String requestedStockCode, LocalDate fallbackDate) {
        Objects.requireNonNull(requestedStockCode, "캔들을 조회한 종목 코드는 필수입니다.");
        Objects.requireNonNull(fallbackDate, "분봉 기준 일자는 필수입니다.");

        if (!SUCCESS_CODE.equals(code)) {
            throw new IllegalStateException("KIS 분봉 조회에 실패했습니다. code=%s, message=%s".formatted(code, message));
        }

        if (candles == null) {
            return List.of();
        }

        return candles.stream()
                .filter(KisMinuteCandle::hasTradeTime)
                .map(candle -> candle.toStockCandle(requestedStockCode, fallbackDate))
                .sorted((left, right) -> left.getCandleTime().compareTo(right.getCandleTime()))
                .toList();
    }

    /**
     * KIS 분봉 output2 단일 캔들.
     *
     * @param businessDate 영업 일자(yyyyMMdd, 비어 있을 수 있음)
     * @param tradeTime 체결 시각(HHmmss)
     * @param openPrice 시가
     * @param highPrice 고가
     * @param lowPrice 저가
     * @param closePrice 종가(분봉 종료 시점 가격)
     * @param tradeVolume 거래량
     * @param tradeAmount 거래대금
     */
    record KisMinuteCandle(
            @JsonProperty("stck_bsop_date")
            String businessDate,

            @JsonProperty("stck_cntg_hour")
            String tradeTime,

            @JsonProperty("stck_oprc")
            String openPrice,

            @JsonProperty("stck_hgpr")
            String highPrice,

            @JsonProperty("stck_lwpr")
            String lowPrice,

            @JsonProperty("stck_prpr")
            String closePrice,

            @JsonProperty("cntg_vol")
            String tradeVolume,

            @JsonProperty("acml_tr_pbmn")
            String tradeAmount
    ) {

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

        private boolean hasTradeTime() {
            return tradeTime != null && tradeTime.trim().length() == 6;
        }

        private StockCandle toStockCandle(String requestedStockCode, LocalDate fallbackDate) {
            LocalDate date = hasBusinessDate()
                    ? LocalDate.parse(businessDate.trim(), DATE_FORMATTER)
                    : fallbackDate;
            LocalTime time = LocalTime.parse(tradeTime.trim(), TIME_FORMATTER);
            LocalDateTime candleTime = LocalDateTime.of(date, time);

            return new StockCandle(
                    requestedStockCode,
                    CandleInterval.MINUTE,
                    candleTime,
                    KisChartParser.parseLong(openPrice),
                    KisChartParser.parseLong(highPrice),
                    KisChartParser.parseLong(lowPrice),
                    KisChartParser.parseLong(closePrice),
                    KisChartParser.parseLong(tradeVolume),
                    KisChartParser.parseLong(tradeAmount)
            );
        }

        private boolean hasBusinessDate() {
            return businessDate != null && businessDate.trim().length() == 8;
        }
    }
}
