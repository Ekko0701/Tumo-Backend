package com.tumo.stock.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tumo.global.error.BusinessException;
import com.tumo.stock.domain.candle.CandleInterval;
import com.tumo.stock.domain.candle.StockCandle;
import com.tumo.stock.dto.StockCandleListResponse;
import com.tumo.stock.port.query.StockCandleQueryPort;
import com.tumo.stock.repository.StockCandleRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

@ExtendWith(MockitoExtension.class)
class StockCandleServiceTest {

    private static final String STOCK_CODE = "005930";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalDate TODAY = LocalDate.of(2025, 2, 1);
    private static final LocalDate FROM = LocalDate.of(2025, 1, 1);
    private static final LocalDate TO = LocalDate.of(2025, 1, 31);

    @Mock
    private StockCandleRepository stockCandleRepository;

    @Mock
    private StockCandleQueryPort stockCandleQueryPort;

    @Mock
    private PlatformTransactionManager transactionManager;

    // TODAY(2025-02-01) KST 정오로 고정해 "오늘" 판정과 미래값 clamp를 결정적으로 검증한다.
    private final Clock clock = Clock.fixed(TODAY.atTime(12, 0).atZone(KST).toInstant(), KST);

    private StockCandleService stockCandleService;

    @BeforeEach
    void setUp() {
        stockCandleService = new StockCandleService(
                stockCandleRepository, stockCandleQueryPort, transactionManager, clock);
    }

    private StockCandle candle(LocalDate date) {
        return new StockCandle(
                STOCK_CODE, CandleInterval.DAY, date.atStartOfDay(),
                53000L, 53800L, 52600L, 53400L, 100L, 500L
        );
    }

    @Test
    void fetchesPersistsAndReturnsFromDb() {
        StockCandle fetched = candle(LocalDate.of(2025, 1, 2));
        given(stockCandleRepository.findTopByStockCodeAndIntervalOrderByCandleTimeAsc(STOCK_CODE, CandleInterval.DAY))
                .willReturn(Optional.empty());
        given(stockCandleQueryPort.findCandles(STOCK_CODE, CandleInterval.DAY, FROM, TO))
                .willReturn(List.of(fetched));
        given(stockCandleRepository.findByStockCodeAndIntervalAndCandleTimeBetweenOrderByCandleTimeAsc(
                eq(STOCK_CODE), eq(CandleInterval.DAY), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(fetched));

        StockCandleListResponse response = stockCandleService.getCandles(STOCK_CODE, CandleInterval.DAY, FROM, TO);

        assertThat(response.candles()).hasSize(1);
        assertThat(response.interval()).isEqualTo("DAY");
        verify(stockCandleRepository).deleteCandleWindow(
                eq(STOCK_CODE), eq(CandleInterval.DAY), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(stockCandleRepository).saveAll(List.of(fetched));
    }

    @Test
    void refetchesTailFromLatestStoredDate() {
        StockCandle latest = candle(LocalDate.of(2025, 1, 20));
        given(stockCandleRepository.findTopByStockCodeAndIntervalOrderByCandleTimeAsc(STOCK_CODE, CandleInterval.DAY))
                .willReturn(Optional.of(candle(FROM)));
        given(stockCandleRepository.findTopByStockCodeAndIntervalOrderByCandleTimeDesc(STOCK_CODE, CandleInterval.DAY))
                .willReturn(Optional.of(latest));
        given(stockCandleQueryPort.findCandles(eq(STOCK_CODE), eq(CandleInterval.DAY), any(LocalDate.class), eq(TO)))
                .willReturn(List.of(latest));
        given(stockCandleRepository.findByStockCodeAndIntervalAndCandleTimeBetweenOrderByCandleTimeAsc(
                eq(STOCK_CODE), eq(CandleInterval.DAY), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(latest));

        stockCandleService.getCandles(STOCK_CODE, CandleInterval.DAY, FROM, TO);

        ArgumentCaptor<LocalDate> fetchFrom = ArgumentCaptor.forClass(LocalDate.class);
        verify(stockCandleQueryPort).findCandles(eq(STOCK_CODE), eq(CandleInterval.DAY), fetchFrom.capture(), eq(TO));
        assertThat(fetchFrom.getValue()).isEqualTo(LocalDate.of(2025, 1, 20));
    }

    @Test
    void refetchesFromStartWhenStoredFrontHasGap() {
        // 저장 데이터가 1/20부터만 있는 상태(앞쪽 1/1~1/19 미보유)에서 1/1~1/31을 요청하면
        // 최신 봉(1/20)이 아니라 from(1/1)부터 다시 받아 앞쪽 구멍을 메워야 한다.
        StockCandle laterOnly = candle(LocalDate.of(2025, 1, 20));
        given(stockCandleRepository.findTopByStockCodeAndIntervalOrderByCandleTimeAsc(STOCK_CODE, CandleInterval.DAY))
                .willReturn(Optional.of(laterOnly));
        given(stockCandleQueryPort.findCandles(eq(STOCK_CODE), eq(CandleInterval.DAY), any(LocalDate.class), eq(TO)))
                .willReturn(List.of(laterOnly));
        given(stockCandleRepository.findByStockCodeAndIntervalAndCandleTimeBetweenOrderByCandleTimeAsc(
                eq(STOCK_CODE), eq(CandleInterval.DAY), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(laterOnly));

        stockCandleService.getCandles(STOCK_CODE, CandleInterval.DAY, FROM, TO);

        ArgumentCaptor<LocalDate> fetchFrom = ArgumentCaptor.forClass(LocalDate.class);
        verify(stockCandleQueryPort).findCandles(eq(STOCK_CODE), eq(CandleInterval.DAY), fetchFrom.capture(), eq(TO));
        assertThat(fetchFrom.getValue()).isEqualTo(FROM);
    }

    @Test
    void fallsBackToDbWhenKisFails() {
        StockCandle stored = candle(LocalDate.of(2025, 1, 10));
        given(stockCandleRepository.findTopByStockCodeAndIntervalOrderByCandleTimeAsc(STOCK_CODE, CandleInterval.DAY))
                .willReturn(Optional.empty());
        given(stockCandleQueryPort.findCandles(STOCK_CODE, CandleInterval.DAY, FROM, TO))
                .willThrow(new RuntimeException("KIS down"));
        given(stockCandleRepository.findByStockCodeAndIntervalAndCandleTimeBetweenOrderByCandleTimeAsc(
                eq(STOCK_CODE), eq(CandleInterval.DAY), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(stored));

        StockCandleListResponse response = stockCandleService.getCandles(STOCK_CODE, CandleInterval.DAY, FROM, TO);

        assertThat(response.candles()).hasSize(1);
        verify(stockCandleRepository, never()).deleteCandleWindow(any(), any(), any(), any());
        verify(stockCandleRepository, never()).saveAll(any());
    }

    @Test
    void doesNotPersistWhenKisReturnsEmpty() {
        given(stockCandleRepository.findTopByStockCodeAndIntervalOrderByCandleTimeAsc(STOCK_CODE, CandleInterval.DAY))
                .willReturn(Optional.empty());
        given(stockCandleQueryPort.findCandles(STOCK_CODE, CandleInterval.DAY, FROM, TO))
                .willReturn(List.of());
        given(stockCandleRepository.findByStockCodeAndIntervalAndCandleTimeBetweenOrderByCandleTimeAsc(
                eq(STOCK_CODE), eq(CandleInterval.DAY), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of());

        StockCandleListResponse response = stockCandleService.getCandles(STOCK_CODE, CandleInterval.DAY, FROM, TO);

        assertThat(response.candles()).isEmpty();
        verify(stockCandleRepository, never()).deleteCandleWindow(any(), any(), any(), any());
        verify(stockCandleRepository, never()).saveAll(any());
    }

    @Test
    void rejectsMinuteRangeExceedingCap() {
        // 분봉은 1회 조회 기간 상한(7일)을 넘으면 KIS를 호출하기 전에 거절해야 한다.
        LocalDate from = TODAY.minusDays(60);

        assertThatThrownBy(() -> stockCandleService.getCandles(STOCK_CODE, CandleInterval.MINUTE, from, TODAY))
                .isInstanceOf(BusinessException.class);

        verify(stockCandleQueryPort, never()).findCandles(any(), any(), any(), any());
    }

    @Test
    void throwsWhenRangeInvalid() {
        assertThatThrownBy(() -> stockCandleService.getCandles(STOCK_CODE, CandleInterval.DAY, TO, FROM))
                .isInstanceOf(BusinessException.class);

        verify(stockCandleQueryPort, never()).findCandles(any(), any(), any(), any());
    }

    @Test
    void rejectsFutureFrom() {
        // 시작 일자가 오늘(KST) 이후면 받을 데이터가 없으므로 거절한다.
        LocalDate from = TODAY.plusDays(1);
        LocalDate to = TODAY.plusDays(2);

        assertThatThrownBy(() -> stockCandleService.getCandles(STOCK_CODE, CandleInterval.DAY, from, to))
                .isInstanceOf(BusinessException.class);

        verify(stockCandleQueryPort, never()).findCandles(any(), any(), any(), any());
    }

    @Test
    void clampsFutureToToToday() {
        // 종료 일자가 미래(오늘 이후)면 오늘(KST)로 잘라 KIS 호출·DB 조회 모두 오늘까지만 수행한다.
        LocalDate futureTo = TODAY.plusDays(10);
        StockCandle fetched = candle(LocalDate.of(2025, 1, 2));
        given(stockCandleRepository.findTopByStockCodeAndIntervalOrderByCandleTimeAsc(STOCK_CODE, CandleInterval.DAY))
                .willReturn(Optional.empty());
        given(stockCandleQueryPort.findCandles(eq(STOCK_CODE), eq(CandleInterval.DAY), eq(FROM), eq(TODAY)))
                .willReturn(List.of(fetched));
        given(stockCandleRepository.findByStockCodeAndIntervalAndCandleTimeBetweenOrderByCandleTimeAsc(
                eq(STOCK_CODE), eq(CandleInterval.DAY), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(fetched));

        stockCandleService.getCandles(STOCK_CODE, CandleInterval.DAY, FROM, futureTo);

        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(stockCandleQueryPort).findCandles(eq(STOCK_CODE), eq(CandleInterval.DAY), eq(FROM), toCaptor.capture());
        assertThat(toCaptor.getValue()).isEqualTo(TODAY);
    }
}
