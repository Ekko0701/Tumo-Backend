package com.tumo.stock.adapter.out.kis.rest.candle;

/**
 * KIS 차트 응답의 문자열 숫자 필드를 파싱하는 공통 유틸리티.
 */
final class KisChartParser {

    private KisChartParser() {
    }

    /**
     * 문자열 숫자를 {@code long}으로 변환한다. 비어 있으면 0을 반환한다.
     *
     * @param value KIS 응답 숫자 문자열
     * @return 변환된 값
     */
    static long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        return Long.parseLong(value.trim());
    }
}
