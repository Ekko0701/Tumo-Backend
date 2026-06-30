package com.tumo.stock.port.query;

import com.tumo.stock.domain.candle.CandleInterval;
import com.tumo.stock.domain.candle.StockCandle;
import java.time.LocalDate;
import java.util.List;

/**
 * 외부 시세 provider로부터 종목 캔들(OHLCV)을 조회하는 outbound port.
 */
public interface StockCandleQueryPort {

    /**
     * 종목 코드와 시간 단위에 해당하는 캔들을 기간 범위로 조회한다.
     *
     * <p>provider 1회 호출 한도를 넘는 기간은 구현체가 내부적으로 나눠 호출해 합친다.</p>
     *
     * @param stockCode 캔들을 조회할 종목 코드
     * @param interval 캔들 시간 단위
     * @param from 조회 시작 일자(포함)
     * @param to 조회 종료 일자(포함)
     * @return 캔들 기준 시각 오름차순 캔들 목록. 데이터가 없으면 빈 목록
     */
    List<StockCandle> findCandles(String stockCode, CandleInterval interval, LocalDate from, LocalDate to);
}
