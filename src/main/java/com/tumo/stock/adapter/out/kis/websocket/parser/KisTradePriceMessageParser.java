package com.tumo.stock.adapter.out.kis.websocket.parser;

import com.tumo.stock.domain.price.StockPrice;
import com.tumo.stock.domain.price.StockPriceEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * KIS 국내주식 실시간체결가 메시지를 Tumo 가격 이벤트로 변환하는 parser.
 */
public class KisTradePriceMessageParser {

    /**
     * KIS WebSocket 메시지의 최상위 구간을 나누는 구분자.
     *
     * <p>예: {@code 0|H0STCNT0|001|...}</p>
     */
    private static final String MESSAGE_DELIMITER = "\\|";

    /**
     * KIS 실시간체결가 payload 내부 필드를 나누는 구분자.
     */
    private static final String DATA_DELIMITER = "\\^";

    /**
     * 최상위 메시지에서 TR ID가 위치하는 index.
     *
     * <p>TR ID를 통해 체결가 메시지인지 확인한다.</p>
     */
    private static final int TR_ID_INDEX = 1;

    /**
     * 최상위 메시지에서 실제 체결가 payload가 위치하는 index.
     */
    private static final int PAYLOAD_INDEX = 3;

    /**
     * payload에서 종목 코드가 위치하는 index.
     */
    private static final int STOCK_CODE_INDEX = 0;

    /**
     * payload에서 체결 시각이 위치하는 index.
     *
     * <p>KIS 실시간체결가 메시지는 날짜 없이 {@code HHmmss} 형태의 시간만 내려준다.</p>
     */
    private static final int TRADE_TIME_INDEX = 1;

    /**
     * payload에서 현재 체결가가 위치하는 index.
     */
    private static final int CURRENT_PRICE_INDEX = 2;

    /**
     * payload에서 전일 대비 가격 변화량이 위치하는 index.
     */
    private static final int CHANGE_PRICE_INDEX = 4;

    /**
     * payload에서 전일 대비 가격 변화율이 위치하는 index.
     */
    private static final int CHANGE_RATE_INDEX = 5;

    /**
     * payload에서 누적 거래량이 위치하는 index.
     *
     * <p>주의: index 12는 "체결 거래량"(이번 체결 틱의 수량)이고, 랭킹에 쓰는 누적 거래량은 index 13이다.</p>
     */
    private static final int ACCUMULATED_VOLUME_INDEX = 13;

    /**
     * payload에서 누적 거래대금이 위치하는 index.
     */
    private static final int ACCUMULATED_TRADE_AMOUNT_INDEX = 14;

    /**
     * KIS 체결 시각을 {@link LocalDateTime}으로 변환할 때 사용하는 formatter.
     *
     * <p>KIS payload의 {@code HHmmss} 앞에 서버 기준 오늘 날짜 {@code yyyyMMdd}를 붙여 사용한다.</p>
     */
    private static final DateTimeFormatter TRADE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * KIS 국내주식 실시간체결가 TR ID.
     */
    private final String tradePriceTrId;

    /**
     * KIS 실시간체결가 메시지 parser를 생성한다.
     *
     * @param tradePriceTrId KIS 국내주식 실시간체결가 TR ID
     */
    public KisTradePriceMessageParser(String tradePriceTrId) {
        if (tradePriceTrId == null || tradePriceTrId.isBlank()) {
            throw new IllegalArgumentException("KIS 체결가 TR ID는 필수입니다.");
        }
        this.tradePriceTrId = tradePriceTrId;
    }

    /**
     * KIS raw message를 가격 이벤트로 변환한다.
     *
     * @param rawMessage KIS WebSocket에서 수신한 원본 메시지
     * @return Tumo 가격 이벤트
     */
    public StockPriceEvent parse(String rawMessage) {
        String payload = extractPayload(rawMessage);
        String[] fields = payload.split(DATA_DELIMITER, -1);

        validateFieldCount(fields);

        StockPrice stockPrice = new StockPrice(
                fields[STOCK_CODE_INDEX],
                parseLong(fields[CURRENT_PRICE_INDEX]),
                parseLong(fields[CHANGE_PRICE_INDEX]),
                parseBigDecimal(fields[CHANGE_RATE_INDEX]),
                parseLong(fields[ACCUMULATED_VOLUME_INDEX]),
                parseLong(fields[ACCUMULATED_TRADE_AMOUNT_INDEX]),
                parseTradeDateTime(fields[TRADE_TIME_INDEX])
        );

        return StockPriceEvent.fromKis(stockPrice, LocalDateTime.now());
    }

    /**
     * KIS raw message에서 실제 체결가 payload 영역만 추출한다.
     *
     * <p>최상위 메시지의 TR ID를 검증해 체결가 메시지가 맞는지도 함께 확인한다.</p>
     *
     * @param rawMessage KIS WebSocket에서 수신한 원본 메시지
     * @return {@code ^} 구분자로 구성된 체결가 payload
     */
    private String extractPayload(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            throw new IllegalArgumentException("KIS 체결가 메시지는 필수입니다.");
        }

        String[] messageParts = rawMessage.split(MESSAGE_DELIMITER, -1);

        if (messageParts.length <= PAYLOAD_INDEX) {
            throw new IllegalArgumentException("KIS 체결가 메시지 형식이 올바르지 않습니다.");
        }

        if (!tradePriceTrId.equals(messageParts[TR_ID_INDEX])) {
            throw new IllegalArgumentException("KIS 체결가 TR ID가 일치하지 않습니다.");
        }

        return messageParts[PAYLOAD_INDEX];
    }

    /**
     * 체결가 payload에 parser가 사용하는 필드들이 모두 포함되어 있는지 검증한다.
     *
     * @param fields {@code ^} 기준으로 분리한 체결가 payload 필드 목록
     */
    private void validateFieldCount(String[] fields) {
        Objects.requireNonNull(fields, "KIS 체결가 payload는 필수입니다.");

        if (fields.length <= ACCUMULATED_TRADE_AMOUNT_INDEX) {
            throw new IllegalArgumentException("KIS 체결가 payload 필드 수가 부족합니다.");
        }
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
     * KIS 소수 문자열을 {@link BigDecimal} 값으로 변환한다.
     *
     * <p>등락률처럼 소수 정밀도가 중요한 값은 부동소수점 오차를 피하기 위해 {@code BigDecimal}을 사용한다.</p>
     *
     * @param value KIS payload에서 추출한 소수 문자열
     * @return 변환된 decimal 값
     */
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(value.trim());
    }

    /**
     * KIS 체결 시각 문자열을 {@link LocalDateTime}으로 변환한다.
     *
     * <p>KIS 실시간체결가 payload는 날짜 없이 {@code HHmmss} 형식의 시간만 제공하므로,
     * 서버 기준 오늘 날짜를 붙여 {@code yyyyMMddHHmmss} 형식으로 변환한다.</p>
     *
     * @param tradeTime KIS payload에서 추출한 체결 시각 문자열
     * @return 날짜가 포함된 체결 시각
     */
    private LocalDateTime parseTradeDateTime(String tradeTime) {
        if (tradeTime == null || tradeTime.isBlank()) {
            return LocalDateTime.now();
        }

        String normalizedTradeTime = tradeTime.trim();

        if (normalizedTradeTime.length() != 6) {
            return LocalDateTime.now();
        }

        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        return LocalDateTime.parse(today + normalizedTradeTime, TRADE_DATE_TIME_FORMATTER);
    }
}
