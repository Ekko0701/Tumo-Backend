package com.tumo.stock.adapter.out.kis.rest.ranking;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tumo.stock.domain.ranking.StockRanking;
import com.tumo.stock.domain.stock.Market;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * KIS 국내주식 랭킹 조회 응답 값.
 *
 * @param code KIS API 처리 결과 코드
 * @param messageCode KIS API 처리 메시지 코드
 * @param message KIS API 처리 메시지
 * @param output KIS 국내주식 랭킹 목록
 */
record KisStockRankingResponse(
        @JsonProperty("rt_cd")
        String code,

        @JsonProperty("msg_cd")
        String messageCode,

        @JsonProperty("msg1")
        String message,

        @JsonProperty("output")
        List<KisStockRankingOutput> output
) {

    /**
     * KIS 정상 처리 코드.
     */
    private static final String SUCCESS_CODE = "0";

    /**
     * KIS 응답을 Tumo 종목 랭킹 목록으로 변환한다.
     *
     * @param market 랭킹을 조회한 시장
     * @return Tumo 종목 랭킹 목록
     */
    List<StockRanking> toStockRankings(Market market) {
        Objects.requireNonNull(market, "시장 구분은 필수입니다.");

        if (!SUCCESS_CODE.equals(code)) {
            throw new IllegalStateException("KIS 종목 랭킹 조회에 실패했습니다. code=%s, message=%s".formatted(code, message));
        }

        if (output == null) {
            return List.of();
        }

        LocalDateTime rankedAt = LocalDateTime.now();
        return output.stream()
                .map(item -> item.toStockRanking(market, rankedAt))
                .toList();
    }

    /**
     * KIS 국내주식 랭킹 output row.
     *
     * @param stockCode 종목 코드
     * @param stockName 종목명
     * @param currentPrice 현재가
     * @param changePrice 전일 대비 가격 변화량
     * @param changeRate 전일 대비 가격 변화율
     * @param tradeVolume 누적 거래량
     * @param tradeAmount 누적 거래대금
     */
    record KisStockRankingOutput(
            @JsonAlias({"mksc_shrn_iscd", "stck_shrn_iscd"})
            String stockCode,

            @JsonProperty("hts_kor_isnm")
            String stockName,

            @JsonProperty("stck_prpr")
            String currentPrice,

            @JsonProperty("prdy_vrss")
            String changePrice,

            @JsonProperty("prdy_ctrt")
            String changeRate,

            @JsonProperty("acml_vol")
            String tradeVolume,

            @JsonProperty("acml_tr_pbmn")
            String tradeAmount
    ) {

        StockRanking toStockRanking(Market market, LocalDateTime rankedAt) {
            return new StockRanking(
                    stockCode,
                    stockName,
                    market,
                    parseLong(currentPrice),
                    parseLong(changePrice),
                    parseBigDecimal(changeRate),
                    parseLong(tradeVolume),
                    parseNullableLong(tradeAmount),
                    rankedAt
            );
        }

        private Long parseLong(String value) {
            if (value == null || value.isBlank()) {
                return 0L;
            }

            return Long.parseLong(value.trim());
        }

        private Long parseNullableLong(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }

            return Long.parseLong(value.trim());
        }

        private BigDecimal parseBigDecimal(String value) {
            if (value == null || value.isBlank()) {
                return BigDecimal.ZERO;
            }

            return new BigDecimal(value.trim());
        }
    }
}
