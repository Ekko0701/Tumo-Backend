package com.tumo.stock.adapter.out.kis.rest.candle;

import com.tumo.stock.adapter.out.kis.rest.client.KisRestClient;
import com.tumo.stock.domain.candle.CandleInterval;
import com.tumo.stock.domain.candle.StockCandle;
import com.tumo.stock.port.query.StockCandleQueryPort;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

/**
 * KIS REST API로 종목 캔들(OHLCV)을 조회하는 outbound adapter.
 *
 * <p>일/주/월/년봉은 국내주식기간별시세 API를, 분봉은 당일/일별 분봉 조회 API를 사용하며,
 * 각 API의 1회 응답 한도를 넘는 구간은 커서를 옮겨가며 반복 호출해 합친다.</p>
 */
public class KisStockCandleQueryClient implements StockCandleQueryPort {

    /**
     * 국내 정규장 개장 시각.
     */
    private static final LocalTime MARKET_OPEN = LocalTime.of(9, 0);

    /**
     * 국내 정규장 마감 시각. 분봉 조회 커서의 시작점으로 사용한다.
     */
    private static final LocalTime MARKET_CLOSE = LocalTime.of(15, 30);

    /**
     * 무한 루프 방지를 위한 페이지 호출 상한.
     */
    private static final int MAX_PAGES = 1_000;

    private final KisRestClient restClient;

    /**
     * 분봉 당일/과거 분기의 "오늘" 판정 기준 Clock. KST 고정 Clock을 주입받아 서버 기본 타임존에 의존하지 않는다.
     */
    private final Clock clock;

    public KisStockCandleQueryClient(KisRestClient restClient, Clock clock) {
        this.restClient = Objects.requireNonNull(restClient, "KIS REST client는 필수입니다.");
        this.clock = Objects.requireNonNull(clock, "Clock은 필수입니다.");
    }

    @Override
    public List<StockCandle> findCandles(String stockCode, CandleInterval interval, LocalDate from, LocalDate to) {
        Objects.requireNonNull(interval, "캔들 시간 단위는 필수입니다.");
        Objects.requireNonNull(from, "조회 시작 일자는 필수입니다.");
        Objects.requireNonNull(to, "조회 종료 일자는 필수입니다.");
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("조회 시작 일자는 종료 일자보다 이후일 수 없습니다.");
        }

        if (interval.isMinute()) {
            return fetchMinuteCandles(stockCode, from, to);
        }
        return fetchPeriodCandles(stockCode, interval, from, to);
    }

    private List<StockCandle> fetchPeriodCandles(String stockCode, CandleInterval interval, LocalDate from, LocalDate to) {
        TreeMap<LocalDateTime, StockCandle> merged = new TreeMap<>();
        LocalDate cursorEnd = to;
        int pages = 0;

        while (!cursorEnd.isBefore(from) && pages++ < MAX_PAGES) {
            KisPeriodChartResponse response = restClient.get(
                    new KisPeriodChartRequest(stockCode, interval, from, cursorEnd).toKisRestRequest()
            );
            List<StockCandle> page = response.toStockCandles(stockCode, interval);
            if (page.isEmpty()) {
                break;
            }
            page.forEach(candle -> merged.putIfAbsent(candle.getCandleTime(), candle));

            LocalDate earliest = page.get(0).getCandleTime().toLocalDate();
            if (!earliest.isAfter(from) || page.size() < KisPeriodChartRequest.MAX_RECORDS_PER_CALL) {
                break;
            }
            cursorEnd = earliest.minusDays(1);
        }

        return merged.values().stream()
                .filter(candle -> isWithinRange(candle.getCandleTime().toLocalDate(), from, to))
                .toList();
    }

    private List<StockCandle> fetchMinuteCandles(String stockCode, LocalDate from, LocalDate to) {
        TreeMap<LocalDateTime, StockCandle> merged = new TreeMap<>();
        LocalDate today = LocalDate.now(clock);

        for (LocalDate date = to; !date.isBefore(from); date = date.minusDays(1)) {
            if (date.isAfter(today)) {
                continue;
            }
            fetchMinuteCandlesForDay(stockCode, date, today)
                    .forEach(candle -> merged.putIfAbsent(candle.getCandleTime(), candle));
        }

        return new ArrayList<>(merged.values());
    }

    private List<StockCandle> fetchMinuteCandlesForDay(String stockCode, LocalDate date, LocalDate today) {
        boolean isToday = date.isEqual(today);
        TreeMap<LocalDateTime, StockCandle> merged = new TreeMap<>();
        LocalTime cursor = MARKET_CLOSE;
        int pages = 0;

        while (pages++ < MAX_PAGES) {
            List<StockCandle> page = isToday
                    ? restClient.get(new KisTodayMinuteChartRequest(stockCode, cursor).toKisRestRequest())
                            .toStockCandles(stockCode, date)
                    : restClient.get(new KisDailyMinuteChartRequest(stockCode, date, cursor).toKisRestRequest())
                            .toStockCandles(stockCode, date);
            if (page.isEmpty()) {
                break;
            }

            int sizeBefore = merged.size();
            page.forEach(candle -> merged.putIfAbsent(candle.getCandleTime(), candle));

            LocalTime earliest = page.get(0).getCandleTime().toLocalTime();
            boolean noProgress = merged.size() == sizeBefore;
            if (!earliest.isAfter(MARKET_OPEN) || noProgress) {
                break;
            }
            cursor = earliest.minusMinutes(1);
            if (cursor.isBefore(MARKET_OPEN)) {
                break;
            }
        }

        return new ArrayList<>(merged.values());
    }

    private static boolean isWithinRange(LocalDate date, LocalDate from, LocalDate to) {
        return !date.isBefore(from) && !date.isAfter(to);
    }
}
