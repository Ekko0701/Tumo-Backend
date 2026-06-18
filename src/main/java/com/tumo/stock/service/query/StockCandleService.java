package com.tumo.stock.service.query;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.domain.candle.CandleInterval;
import com.tumo.stock.domain.candle.StockCandle;
import com.tumo.stock.dto.StockCandleListResponse;
import com.tumo.stock.port.query.StockCandleQueryPort;
import com.tumo.stock.repository.StockCandleRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 종목 캔들(차트) 조회 서비스.
 *
 * <p>DB에 저장된 캔들을 우선 사용하고, 누락 구간과 미완성 최신 봉만 KIS로 보충하는 cache-aside 방식으로 동작한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockCandleService {

    private final StockCandleRepository stockCandleRepository;
    private final StockCandleQueryPort stockCandleQueryPort;

    /**
     * 종목·시간 단위·기간에 해당하는 캔들을 조회한다.
     *
     * @param stockCode 종목 코드
     * @param interval 캔들 시간 단위
     * @param from 조회 시작 일자(포함)
     * @param to 조회 종료 일자(포함)
     * @return 캔들 기준 시각 오름차순 캔들 목록 응답
     */
    @Transactional
    public StockCandleListResponse getCandles(String stockCode, CandleInterval interval, LocalDate from, LocalDate to) {
        validateRange(from, to);
        synchronizeCandles(stockCode, interval, from, to);

        List<StockCandle> candles = stockCandleRepository
                .findByStockCodeAndIntervalAndCandleTimeBetweenOrderByCandleTimeAsc(
                        stockCode,
                        interval,
                        from.atStartOfDay(),
                        to.atTime(LocalTime.MAX)
                );

        return StockCandleListResponse.of(stockCode, interval, candles);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to) || from.isAfter(LocalDate.now())) {
            throw new BusinessException(ErrorCode.INVALID_CANDLE_RANGE);
        }
    }

    /**
     * 저장된 최신 봉 이후 구간(미완성 최신 봉 포함)을 KIS에서 다시 받아 DB를 보충한다.
     * KIS 조회에 실패하면 DB 보유분으로 폴백한다.
     */
    private void synchronizeCandles(String stockCode, CandleInterval interval, LocalDate from, LocalDate to) {
        LocalDate fetchFrom = resolveFetchStart(stockCode, interval, from);
        try {
            List<StockCandle> fetched = stockCandleQueryPort.findCandles(stockCode, interval, fetchFrom, to);
            if (fetched.isEmpty()) {
                return;
            }
            replaceWindow(stockCode, interval, fetchFrom, to, fetched);
        } catch (RuntimeException exception) {
            log.warn(
                    "KIS 캔들 조회에 실패하여 DB 보유분으로 폴백합니다. stockCode={}, interval={}, from={}, to={}",
                    stockCode, interval, fetchFrom, to, exception
            );
        }
    }

    /**
     * KIS 재조회 시작 일자를 결정한다.
     *
     * <p>저장된 가장 이른 봉이 조회 시작일({@code from})을 덮지 못하면(앞쪽에 구멍이 있거나 저장 데이터가 없으면)
     * {@code from}부터 전체 구간을 다시 받아 구멍을 메운다. 앞쪽이 이미 채워져 있으면, 저장된 최신 봉부터만 다시 받아
     * 미완성 최신 봉을 갱신한다.</p>
     *
     * <p>주의: 앞쪽이 채워진 상태에서 중간에 끊긴 구간(서로 떨어진 저장 구간)은 이 방식으로 메워지지 않는다.
     * 차트 UI처럼 범위를 이어서 넓혀 보는 접근 패턴에서는 발생하지 않지만, 분리된 구간을 따로 조회하는 경우라면
     * 전체 구간 재조회로 전환해야 한다.</p>
     */
    private LocalDate resolveFetchStart(String stockCode, CandleInterval interval, LocalDate from) {
        boolean frontCovered = stockCandleRepository
                .findTopByStockCodeAndIntervalOrderByCandleTimeAsc(stockCode, interval)
                .map(earliest -> earliest.getCandleTime().toLocalDate())
                .map(earliestDate -> !earliestDate.isAfter(from))
                .orElse(false);

        if (!frontCovered) {
            return from;
        }

        return stockCandleRepository.findTopByStockCodeAndIntervalOrderByCandleTimeDesc(stockCode, interval)
                .map(latest -> latest.getCandleTime().toLocalDate())
                .filter(latestDate -> latestDate.isAfter(from))
                .orElse(from);
    }

    /**
     * 재조회 구간의 기존 캔들을 삭제하고 새로 받은 캔들로 교체한다.
     */
    private void replaceWindow(
            String stockCode,
            CandleInterval interval,
            LocalDate fetchFrom,
            LocalDate to,
            List<StockCandle> fetched
    ) {
        stockCandleRepository.deleteCandleWindow(
                stockCode,
                interval,
                fetchFrom.atStartOfDay(),
                to.atTime(LocalTime.MAX)
        );
        stockCandleRepository.saveAll(fetched);
    }
}
