package com.tumo.stock.adapter.out.kis.rest.price;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tumo.stock.domain.price.StockPrice;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * KIS 국내주식 현재가 조회 응답 값.
 *
 * @param code KIS API 처리 결과 코드
 * @param messageCode KIS API 처리 메시지 코드
 * @param message KIS API 처리 메시지
 * @param output KIS 국내주식 현재가 상세 응답 값
 */
record KisInquirePriceResponse(
        @JsonProperty("rt_cd")
        String code,

        @JsonProperty("msg_cd")
        String messageCode,

        @JsonProperty("msg1")
        String message,

        @JsonProperty("output")
        KisInquirePriceOutput output
) {

    /**
     * KIS 정상 처리 코드.
     */
    private static final String SUCCESS_CODE = "0";

    /**
     * KIS 응답을 Tumo 가격 값 객체로 변환한다.
     *
     * @param requestedStockCode 현재가 조회를 요청한 종목 코드
     * @return Tumo 가격 값 객체
     */
    StockPrice toStockPrice(String requestedStockCode) {
        Objects.requireNonNull(requestedStockCode, "현재가를 조회한 종목 코드는 필수입니다.");

        if (!SUCCESS_CODE.equals(code)) {
            throw new IllegalStateException("KIS 현재가 조회에 실패했습니다. code=%s, message=%s".formatted(code, message));
        }

        if (output == null) {
            throw new IllegalStateException("KIS 현재가 조회 응답이 비어 있습니다.");
        }

        return output.toStockPrice(requestedStockCode);
    }

    /**
     * KIS 국내주식 현재가 조회 output 영역.
     *
     * @param currentPrice 현재가
     * @param changePrice 전일 대비 가격 변화량
     * @param changeRate 전일 대비 가격 변화율
     * @param tradeVolume 누적 거래량
     * @param tradeAmount 누적 거래대금
     * @param businessDate 영업 일자
     * @param tradeTime 체결 시각
     */
    record KisInquirePriceOutput(
            @JsonProperty("stck_prpr")
            String currentPrice,

            @JsonProperty("prdy_vrss")
            String changePrice,

            @JsonProperty("prdy_ctrt")
            String changeRate,

            @JsonProperty("acml_vol")
            String tradeVolume,

            @JsonProperty("acml_tr_pbmn")
            String tradeAmount,

            @JsonProperty("stck_bsop_date")
            String businessDate,

            @JsonProperty("stck_cntg_hour")
            String tradeTime
    ) {

        /**
         * KIS 날짜와 시간 문자열을 변환할 때 사용하는 formatter.
         */
        private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        /**
         * KIS output을 Tumo 가격 값 객체로 변환한다.
         *
         * @param requestedStockCode 현재가 조회를 요청한 종목 코드
         * @return Tumo 가격 값 객체
         */
        StockPrice toStockPrice(String requestedStockCode) {
            return new StockPrice(
                    requestedStockCode,
                    parseLong(currentPrice),
                    parseLong(changePrice),
                    parseBigDecimal(changeRate),
                    parseLong(tradeVolume),
                    parseLong(tradeAmount),
                    parsePriceChangedAt()
            );
        }

        private Long parseLong(String value) {
            if (value == null || value.isBlank()) {
                return 0L;
            }

            return Long.parseLong(value.trim());
        }

        private BigDecimal parseBigDecimal(String value) {
            if (value == null || value.isBlank()) {
                return BigDecimal.ZERO;
            }

            return new BigDecimal(value.trim());
        }

        private LocalDateTime parsePriceChangedAt() {
            if (businessDate == null || businessDate.isBlank() || tradeTime == null || tradeTime.isBlank()) {
                return LocalDateTime.now();
            }

            String normalizedBusinessDate = businessDate.trim();
            String normalizedTradeTime = tradeTime.trim();

            if (normalizedBusinessDate.length() != 8 || normalizedTradeTime.length() != 6) {
                return LocalDateTime.now();
            }

            return LocalDateTime.parse(normalizedBusinessDate + normalizedTradeTime, DATE_TIME_FORMATTER);
        }
    }
}
