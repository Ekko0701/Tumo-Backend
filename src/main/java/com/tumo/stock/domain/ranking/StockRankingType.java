package com.tumo.stock.domain.ranking;

/**
 * 종목 목록의 랭킹 기준.
 */
public enum StockRankingType {

    /**
     * 거래대금 상위 랭킹.
     */
    TRADE_AMOUNT,

    /**
     * 거래량 상위 랭킹.
     */
    TRADE_VOLUME,

    /**
     * 전일 대비 급상승 랭킹.
     */
    RISING,

    /**
     * 전일 대비 급하락 랭킹.
     */
    FALLING,

    /**
     * Tumo 내부 사용자 행동 데이터 기반 인기 랭킹.
     */
    POPULAR
}
