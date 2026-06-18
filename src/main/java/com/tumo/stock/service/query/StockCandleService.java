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
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 종목 캔들(차트) 조회 서비스.
 *
 * <p>DB에 저장된 캔들을 우선 사용하고, 누락 구간과 미완성 최신 봉만 KIS로 보충하는 cache-aside 방식으로 동작한다.</p>
 *
 * <p>외부 KIS 호출(분봉은 수십 회 반복될 수 있음)은 DB 트랜잭션 밖에서 수행하고, DB 쓰기(삭제 후 삽입)만
 * 짧은 트랜잭션으로 감싼다. 느린 네트워크 작업 동안 DB 커넥션을 점유해 풀이 고갈되는 것을 막기 위함이다.</p>
 */
@Slf4j
@Service
public class StockCandleService {

    /**
     * 분봉 1회 조회 허용 최대 기간(일). 넓은 분봉 요청이 KIS 호출 폭증을 일으키지 않도록 제한한다.
     * 차트 화면에 한 번에 보이는 분봉(약 1.5거래일)의 넉넉한 배수로 잡아 스크롤 프리페치를 충분히 허용한다.
     */
    private static final long MAX_MINUTE_RANGE_DAYS = 7;

    private final StockCandleRepository stockCandleRepository;
    private final StockCandleQueryPort stockCandleQueryPort;

    /**
     * DB 쓰기(삭제 후 삽입)를 원자적으로 묶기 위한 트랜잭션 실행기. KIS 호출과 분리해 트랜잭션을 짧게 유지한다.
     */
    private final TransactionTemplate transactionTemplate;

    public StockCandleService(
            StockCandleRepository stockCandleRepository,
            StockCandleQueryPort stockCandleQueryPort,
            PlatformTransactionManager transactionManager
    ) {
        this.stockCandleRepository = stockCandleRepository;
        this.stockCandleQueryPort = stockCandleQueryPort;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * 종목·시간 단위·기간에 해당하는 캔들을 조회한다.
     *
     * <p>① DB를 조회해 KIS 재조회 시작점을 정하고 → ② 트랜잭션 밖에서 KIS를 호출하고 →
     * ③ 받은 구간만 짧은 트랜잭션으로 DB에 교체 저장한 뒤 → ④ DB에서 최종 결과를 읽어 반환한다.</p>
     *
     * @param stockCode 종목 코드
     * @param interval 캔들 시간 단위
     * @param from 조회 시작 일자(포함)
     * @param to 조회 종료 일자(포함)
     * @return 캔들 기준 시각 오름차순 캔들 목록 응답
     */
    public StockCandleListResponse getCandles(String stockCode, CandleInterval interval, LocalDate from, LocalDate to) {
        validateRange(interval, from, to);

        LocalDate fetchFrom = resolveFetchStart(stockCode, interval, from);
        List<StockCandle> fetched = fetchFromKis(stockCode, interval, fetchFrom, to);
        if (!fetched.isEmpty()) {
            replaceWindow(stockCode, interval, fetchFrom, to, fetched);
        }

        List<StockCandle> candles = stockCandleRepository
                .findByStockCodeAndIntervalAndCandleTimeBetweenOrderByCandleTimeAsc(
                        stockCode,
                        interval,
                        from.atStartOfDay(),
                        to.atTime(LocalTime.MAX)
                );

        return StockCandleListResponse.of(stockCode, interval, candles);
    }

    private void validateRange(CandleInterval interval, LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to) || from.isAfter(LocalDate.now())) {
            throw new BusinessException(ErrorCode.INVALID_CANDLE_RANGE);
        }
        // 분봉은 하루당 KIS 호출이 많아, 넓은 기간 요청 시 호출이 폭증한다. 1회 조회 기간을 제한한다.
        if (interval.isMinute() && ChronoUnit.DAYS.between(from, to) > MAX_MINUTE_RANGE_DAYS) {
            throw new BusinessException(ErrorCode.INVALID_CANDLE_RANGE);
        }
    }

    /**
     * KIS에서 캔들을 조회한다. 외부 HTTP 호출이므로 DB 트랜잭션 밖에서 수행하며,
     * 실패 시 빈 목록을 반환해 호출 측이 DB 보유분으로 폴백하게 한다.
     */
    private List<StockCandle> fetchFromKis(String stockCode, CandleInterval interval, LocalDate fetchFrom, LocalDate to) {
        try {
            return stockCandleQueryPort.findCandles(stockCode, interval, fetchFrom, to);
        } catch (RuntimeException exception) {
            log.warn(
                    "KIS 캔들 조회에 실패하여 DB 보유분으로 폴백합니다. stockCode={}, interval={}, from={}, to={}",
                    stockCode, interval, fetchFrom, to, exception
            );
            return List.of();
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
     * 삭제와 삽입을 한 트랜잭션으로 묶어 원자적으로 수행하되, KIS 호출과는 분리해 트랜잭션을 짧게 유지한다.
     */
    private void replaceWindow(
            String stockCode,
            CandleInterval interval,
            LocalDate fetchFrom,
            LocalDate to,
            List<StockCandle> fetched
    ) {
        transactionTemplate.executeWithoutResult(status -> {
            stockCandleRepository.deleteCandleWindow(
                    stockCode,
                    interval,
                    fetchFrom.atStartOfDay(),
                    to.atTime(LocalTime.MAX)
            );
            stockCandleRepository.saveAll(fetched);
        });
    }
}
