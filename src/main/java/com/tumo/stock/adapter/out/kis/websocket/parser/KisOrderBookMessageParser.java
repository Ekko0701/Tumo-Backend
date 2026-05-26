package com.tumo.stock.adapter.out.kis.websocket.parser;

import com.tumo.stock.domain.orderbook.StockOrderBook;
import com.tumo.stock.domain.orderbook.StockOrderBookEvent;
import com.tumo.stock.domain.orderbook.StockOrderBookLevel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * KIS 국내주식 실시간호가 메시지를 Tumo 호가 이벤트로 변환하는 parser.
 */
public class KisOrderBookMessageParser {

    /**
     * KIS WebSocket 메시지의 최상위 구간을 나누는 구분자.
     *
     * <p>예: {@code 0|H0STASP0|001|...}</p>
     */
    private static final String MESSAGE_DELIMITER = "\\|";

    /**
     * KIS 실시간호가 payload 내부 필드를 나누는 구분자.
     */
    private static final String DATA_DELIMITER = "\\^";

    /**
     * 최상위 메시지에서 TR ID가 위치하는 index.
     *
     * <p>TR ID를 통해 호가 메시지인지 확인한다.</p>
     */
    private static final int TR_ID_INDEX = 1;

    /**
     * 최상위 메시지에서 실제 호가 payload가 위치하는 index.
     */
    private static final int PAYLOAD_INDEX = 3;

    /**
     * payload에서 종목 코드가 위치하는 index.
     */
    private static final int STOCK_CODE_INDEX = 0;

    /**
     * payload에서 호가 변경 시각이 위치하는 index.
     *
     * <p>KIS 실시간호가 메시지는 날짜 없이 {@code HHmmss} 형태의 시간만 내려준다.</p>
     */
    private static final int ORDER_BOOK_TIME_INDEX = 1;

    /**
     * payload에서 매도 호가 가격 목록이 시작되는 index.
     */
    private static final int ASK_PRICE_START_INDEX = 3;

    /**
     * payload에서 매수 호가 가격 목록이 시작되는 index.
     */
    private static final int BID_PRICE_START_INDEX = 13;

    /**
     * payload에서 매도 호가 잔량 목록이 시작되는 index.
     */
    private static final int ASK_VOLUME_START_INDEX = 23;

    /**
     * payload에서 매수 호가 잔량 목록이 시작되는 index.
     */
    private static final int BID_VOLUME_START_INDEX = 33;

    /**
     * KIS 국내주식 실시간호가에서 사용하는 호가 단계 수.
     */
    private static final int ORDER_BOOK_LEVEL_COUNT = 10;

    /**
     * KIS 호가 변경 시각을 {@link LocalDateTime}으로 변환할 때 사용하는 formatter.
     *
     * <p>KIS payload의 {@code HHmmss} 앞에 서버 기준 오늘 날짜 {@code yyyyMMdd}를 붙여 사용한다.</p>
     */
    private static final DateTimeFormatter ORDER_BOOK_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * KIS 국내주식 실시간호가 TR ID.
     */
    private final String orderBookTrId;

    /**
     * KIS 실시간호가 메시지 parser를 생성한다.
     *
     * @param orderBookTrId KIS 국내주식 실시간호가 TR ID
     */
    public KisOrderBookMessageParser(String orderBookTrId) {
        if (orderBookTrId == null || orderBookTrId.isBlank()) {
            throw new IllegalArgumentException("KIS 호가 TR ID는 필수입니다.");
        }
        this.orderBookTrId = orderBookTrId;
    }

    /**
     * KIS raw message를 호가 이벤트로 변환한다.
     *
     * @param rawMessage KIS WebSocket에서 수신한 원본 메시지
     * @return Tumo 호가 이벤트
     */
    public StockOrderBookEvent parse(String rawMessage) {
        String payload = extractPayload(rawMessage);
        String[] fields = payload.split(DATA_DELIMITER, -1);

        validateFieldCount(fields);

        StockOrderBook orderBook = new StockOrderBook(
                fields[STOCK_CODE_INDEX],
                parseAskLevels(fields),
                parseBidLevels(fields),
                parseOrderBookDateTime(fields[ORDER_BOOK_TIME_INDEX])
        );

        return StockOrderBookEvent.fromKis(orderBook, LocalDateTime.now());
    }

    /**
     * KIS raw message에서 실제 호가 payload 영역만 추출한다.
     *
     * <p>최상위 메시지의 TR ID를 검증해 호가 메시지가 맞는지도 함께 확인한다.</p>
     *
     * @param rawMessage KIS WebSocket에서 수신한 원본 메시지
     * @return {@code ^} 구분자로 구성된 호가 payload
     */
    private String extractPayload(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            throw new IllegalArgumentException("KIS 호가 메시지는 필수입니다.");
        }

        String[] messageParts = rawMessage.split(MESSAGE_DELIMITER, -1);

        if (messageParts.length <= PAYLOAD_INDEX) {
            throw new IllegalArgumentException("KIS 호가 메시지 형식이 올바르지 않습니다.");
        }

        if (!orderBookTrId.equals(messageParts[TR_ID_INDEX])) {
            throw new IllegalArgumentException("KIS 호가 TR ID가 일치하지 않습니다.");
        }

        return messageParts[PAYLOAD_INDEX];
    }

    /**
     * 호가 payload에 parser가 사용하는 필드들이 모두 포함되어 있는지 검증한다.
     *
     * @param fields {@code ^} 기준으로 분리한 호가 payload 필드 목록
     */
    private void validateFieldCount(String[] fields) {
        Objects.requireNonNull(fields, "KIS 호가 payload는 필수입니다.");

        int requiredLastIndex = BID_VOLUME_START_INDEX + ORDER_BOOK_LEVEL_COUNT - 1;

        if (fields.length <= requiredLastIndex) {
            throw new IllegalArgumentException("KIS 호가 payload 필드 수가 부족합니다.");
        }
    }

    /**
     * payload에서 매도 호가 10단계 가격과 잔량을 추출한다.
     *
     * @param fields {@code ^} 기준으로 분리한 호가 payload 필드 목록
     * @return 매도 호가 레벨 목록
     */
    private List<StockOrderBookLevel> parseAskLevels(String[] fields) {
        List<StockOrderBookLevel> levels = new ArrayList<>();

        for (int index = 0; index < ORDER_BOOK_LEVEL_COUNT; index++) {
            Long price = parseLong(fields[ASK_PRICE_START_INDEX + index]);
            Long volume = parseLong(fields[ASK_VOLUME_START_INDEX + index]);

            levels.add(new StockOrderBookLevel(price, volume));
        }

        return levels;
    }

    /**
     * payload에서 매수 호가 10단계 가격과 잔량을 추출한다.
     *
     * @param fields {@code ^} 기준으로 분리한 호가 payload 필드 목록
     * @return 매수 호가 레벨 목록
     */
    private List<StockOrderBookLevel> parseBidLevels(String[] fields) {
        List<StockOrderBookLevel> levels = new ArrayList<>();

        for (int index = 0; index < ORDER_BOOK_LEVEL_COUNT; index++) {
            Long price = parseLong(fields[BID_PRICE_START_INDEX + index]);
            Long volume = parseLong(fields[BID_VOLUME_START_INDEX + index]);

            levels.add(new StockOrderBookLevel(price, volume));
        }

        return levels;
    }

    /**
     * KIS 숫자 문자열을 {@link Long} 값으로 변환한다.
     *
     * <p>비어 있는 값은 외부 응답 누락을 허용하기 위해 {@code 0}으로 처리한다.</p>
     *
     * @param value KIS payload에서 추출한 숫자 문자열
     * @return 변환된 long 값
     */
    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }

        return Long.parseLong(value.trim());
    }

    /**
     * KIS 호가 변경 시각 문자열을 {@link LocalDateTime}으로 변환한다.
     *
     * <p>KIS 실시간호가 payload는 날짜 없이 {@code HHmmss} 형식의 시간만 제공하므로,
     * 서버 기준 오늘 날짜를 붙여 {@code yyyyMMddHHmmss} 형식으로 변환한다.</p>
     *
     * @param orderBookTime KIS payload에서 추출한 호가 변경 시각 문자열
     * @return 날짜가 포함된 호가 변경 시각
     */
    private LocalDateTime parseOrderBookDateTime(String orderBookTime) {
        if (orderBookTime == null || orderBookTime.isBlank()) {
            return LocalDateTime.now();
        }

        String normalizedOrderBookTime = orderBookTime.trim();

        if (normalizedOrderBookTime.length() != 6) {
            return LocalDateTime.now();
        }

        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        return LocalDateTime.parse(today + normalizedOrderBookTime, ORDER_BOOK_DATE_TIME_FORMATTER);
    }
}
