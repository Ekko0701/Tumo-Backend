package com.tumo.stock.domain.candle;

/**
 * 차트 캔들의 시간 단위를 표현하는 enum.
 *
 * <p>일/주/월/년봉은 KIS 국내주식기간별시세 API의 기간 분류 코드를 가지며, 분봉은 별도의 분봉 조회 API를 사용한다.</p>
 */
public enum CandleInterval {

    /**
     * 분봉. KIS 당일/일별 분봉 조회 API를 사용한다.
     */
    MINUTE(null),

    /**
     * 일봉.
     */
    DAY("D"),

    /**
     * 주봉.
     */
    WEEK("W"),

    /**
     * 월봉.
     */
    MONTH("M"),

    /**
     * 년봉.
     */
    YEAR("Y");

    /**
     * KIS 국내주식기간별시세 API의 기간 분류 코드. 분봉은 사용하지 않으므로 {@code null}.
     */
    private final String kisPeriodCode;

    CandleInterval(String kisPeriodCode) {
        this.kisPeriodCode = kisPeriodCode;
    }

    /**
     * 분봉 여부를 반환한다.
     *
     * @return 분봉이면 {@code true}
     */
    public boolean isMinute() {
        return this == MINUTE;
    }

    /**
     * KIS 국내주식기간별시세 API에 사용할 기간 분류 코드를 반환한다.
     *
     * @return 기간 분류 코드(D/W/M/Y)
     * @throws IllegalStateException 분봉처럼 기간 분류 코드가 없는 경우
     */
    public String kisPeriodCode() {
        if (kisPeriodCode == null) {
            throw new IllegalStateException("%s 캔들은 기간별시세 기간 분류 코드를 사용하지 않습니다.".formatted(name()));
        }
        return kisPeriodCode;
    }
}
