package com.tumo.stock.adapter.out.noop;

import com.tumo.stock.domain.candle.CandleInterval;
import com.tumo.stock.domain.candle.StockCandle;
import com.tumo.stock.port.query.StockCandleQueryPort;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 실제 캔들 조회 provider가 설정되지 않았을 때 빈 결과를 반환하는 기본 query adapter.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoopStockCandleQueryClient implements StockCandleQueryPort {

    /**
     * 외부 provider를 호출하지 않고 빈 캔들 목록을 반환한다.
     *
     * @param stockCode 캔들을 조회할 종목 코드
     * @param interval 캔들 시간 단위
     * @param from 조회 시작 일자
     * @param to 조회 종료 일자
     * @return 빈 캔들 목록
     */
    @Override
    public List<StockCandle> findCandles(String stockCode, CandleInterval interval, LocalDate from, LocalDate to) {
        log.debug(
                "Stock candle query client is not configured yet. stockCode={}, interval={}, from={}, to={}",
                stockCode, interval, from, to
        );
        return List.of();
    }
}
