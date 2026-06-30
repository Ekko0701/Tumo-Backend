package com.tumo.stock.adapter.out.kis.rest.candle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.tumo.stock.adapter.out.kis.rest.candle.KisMinuteChartResponse.KisMinuteCandle;
import com.tumo.stock.adapter.out.kis.rest.candle.KisPeriodChartResponse.KisPeriodCandle;
import com.tumo.stock.adapter.out.kis.rest.client.KisRestClient;
import com.tumo.stock.adapter.out.kis.rest.client.KisRestRequest;
import com.tumo.stock.domain.candle.CandleInterval;
import com.tumo.stock.domain.candle.StockCandle;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KisStockCandleQueryClientTest {

    private static final String STOCK_CODE = "005930";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // KIS 분봉 API의 거래 ID. 당일/과거 분기 검증에 사용한다.
    private static final String TRID_TODAY_MINUTE = "FHKST03010200";
    private static final String TRID_PAST_MINUTE = "FHKST03010230";

    @Mock
    private KisRestClient restClient;

    // 각 호출이 사용한 KIS 거래 ID를 순서대로 기록한다(당일/과거 분기 검증용).
    private final List<String> requestedTransactionIds = new ArrayList<>();

    private KisStockCandleQueryClient client(LocalDate today) {
        Clock clock = Clock.fixed(today.atTime(12, 0).atZone(KST).toInstant(), KST);
        return new KisStockCandleQueryClient(restClient, clock);
    }

    @Test
    void requiresNonNullClock() {
        assertThatThrownBy(() -> new KisStockCandleQueryClient(restClient, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Clock");
    }

    @Test
    void throwsWhenFromIsAfterTo() {
        KisStockCandleQueryClient client = client(LocalDate.of(2025, 6, 16));

        assertThatThrownBy(() -> client.findCandles(
                STOCK_CODE, CandleInterval.DAY, LocalDate.of(2025, 6, 16), LocalDate.of(2025, 6, 15)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void usesTodayMinuteApiWhenDateIsToday() {
        // 주입한 Clock 기준 "오늘"인 날짜는 당일분봉 API(FHKST03010200)를 사용해야 한다.
        LocalDate today = LocalDate.of(2025, 6, 16);
        KisStockCandleQueryClient client = client(today);
        recordTransactionIdsAnswering(minuteResponse(today, "090000"));

        client.findCandles(STOCK_CODE, CandleInterval.MINUTE, today, today);

        assertThat(requestedTransactionIds).containsExactly(TRID_TODAY_MINUTE);
    }

    @Test
    void usesPastMinuteApiWhenDateIsBeforeToday() {
        // 주입한 Clock 기준 "오늘 이전"인 날짜는 과거분봉 API(FHKST03010230)를 사용해야 한다.
        // 서버 기본 타임존이 아니라 주입한 Clock으로 분기됨을 검증한다.
        LocalDate date = LocalDate.of(2025, 6, 16);
        LocalDate today = LocalDate.of(2025, 6, 17);
        KisStockCandleQueryClient client = client(today);
        recordTransactionIdsAnswering(minuteResponse(date, "090000"));

        client.findCandles(STOCK_CODE, CandleInterval.MINUTE, date, date);

        assertThat(requestedTransactionIds).containsExactly(TRID_PAST_MINUTE);
    }

    @Test
    void skipsFutureDatesInMinuteRange() {
        // to가 미래여도 클라이언트는 오늘 이후 날짜를 건너뛰고 오늘까지만 호출한다.
        LocalDate today = LocalDate.of(2025, 6, 16);
        KisStockCandleQueryClient client = client(today);
        recordTransactionIdsAnswering(minuteResponse(today, "090000"));

        client.findCandles(STOCK_CODE, CandleInterval.MINUTE, today, today.plusDays(2));

        // 미래 2일은 호출되지 않고 오늘(당일분봉) 1회만 호출된다.
        assertThat(requestedTransactionIds).containsExactly(TRID_TODAY_MINUTE);
    }

    @Test
    void fetchesPeriodCandlesSinglePage() {
        KisStockCandleQueryClient client = client(LocalDate.of(2025, 6, 16));
        given(restClient.get(any())).willReturn(
                periodResponse(List.of(periodRow("20250102"), periodRow("20250103"))));

        List<StockCandle> candles = client.findCandles(
                STOCK_CODE, CandleInterval.DAY, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 3));

        assertThat(candles).hasSize(2);
        assertThat(candles.get(0).getCandleTime()).isEqualTo("2025-01-02T00:00:00");
        assertThat(candles.get(1).getCandleTime()).isEqualTo("2025-01-03T00:00:00");
        verify(restClient, times(1)).get(any());
    }

    @Test
    void paginatesPeriodCandlesUntilFromReached() {
        // 1회 응답 한도(100건)를 가득 채우고 가장 이른 봉이 아직 from보다 뒤면 커서를 옮겨 다시 호출한다.
        KisStockCandleQueryClient client = client(LocalDate.of(2025, 6, 16));

        List<KisPeriodCandle> firstPage = new ArrayList<>();
        LocalDate base = LocalDate.of(2025, 3, 1);
        for (int i = 0; i < KisPeriodChartRequest.MAX_RECORDS_PER_CALL; i++) {
            firstPage.add(periodRow(base.plusDays(i).format(DateTimeFormatter.BASIC_ISO_DATE)));
        }

        AtomicInteger calls = new AtomicInteger();
        given(restClient.get(any())).willAnswer(invocation ->
                calls.getAndIncrement() == 0
                        ? periodResponse(firstPage)
                        : periodResponse(List.of(periodRow("20250101"))));

        List<StockCandle> candles = client.findCandles(
                STOCK_CODE, CandleInterval.DAY, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        assertThat(candles).hasSize(KisPeriodChartRequest.MAX_RECORDS_PER_CALL + 1);
        verify(restClient, times(2)).get(any());
    }

    private void recordTransactionIdsAnswering(Object response) {
        given(restClient.get(any())).willAnswer(invocation -> {
            KisRestRequest<?> request = invocation.getArgument(0);
            requestedTransactionIds.add(request.transactionId());
            return response;
        });
    }

    private KisMinuteChartResponse minuteResponse(LocalDate date, String time) {
        return new KisMinuteChartResponse(
                "0",
                "정상처리 되었습니다.",
                List.of(new KisMinuteCandle(
                        date.format(DateTimeFormatter.BASIC_ISO_DATE), time,
                        "53000", "53800", "52600", "53400", "1500", "80000000"))
        );
    }

    private KisPeriodChartResponse periodResponse(List<KisPeriodCandle> rows) {
        return new KisPeriodChartResponse("0", "정상처리 되었습니다.", rows);
    }

    private KisPeriodCandle periodRow(String businessDate) {
        return new KisPeriodCandle(businessDate, "53000", "53800", "52600", "53400", "100", "500");
    }
}
