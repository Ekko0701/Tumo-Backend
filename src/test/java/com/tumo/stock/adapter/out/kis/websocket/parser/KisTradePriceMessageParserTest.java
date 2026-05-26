package com.tumo.stock.adapter.out.kis.websocket.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tumo.stock.domain.price.StockPriceEvent;
import org.junit.jupiter.api.Test;

class KisTradePriceMessageParserTest {

    private final KisTradePriceMessageParser parser = new KisTradePriceMessageParser("H0STCNT0");

    @Test
    void parseTradePriceMessage() {
        String rawMessage = "0|H0STCNT0|001|" + tradePricePayload();

        StockPriceEvent event = parser.parse(rawMessage);

        assertThat(event.provider()).isEqualTo("KIS");
        assertThat(event.price().stockCode()).isEqualTo("005930");
        assertThat(event.price().currentPrice()).isEqualTo(75100L);
        assertThat(event.price().changePrice()).isEqualTo(100L);
        assertThat(event.price().changeRate()).isEqualByComparingTo("0.13");
        assertThat(event.price().tradeVolume()).isEqualTo(1234567L);
        assertThat(event.price().priceChangedAt().getHour()).isEqualTo(9);
        assertThat(event.price().priceChangedAt().getMinute()).isEqualTo(30);
    }

    @Test
    void throwsExceptionWhenTrIdDoesNotMatch() {
        String rawMessage = "0|H0STASP0|001|" + tradePricePayload();

        assertThatThrownBy(() -> parser.parse(rawMessage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("KIS 체결가 TR ID가 일치하지 않습니다.");
    }

    @Test
    void throwsExceptionWhenFieldCountIsNotEnough() {
        String rawMessage = "0|H0STCNT0|001|005930^093000^75100";

        assertThatThrownBy(() -> parser.parse(rawMessage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("KIS 체결가 payload 필드 수가 부족합니다.");
    }

    @Test
    void throwsExceptionWhenRawMessageIsBlank() {
        assertThatThrownBy(() -> parser.parse(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("KIS 체결가 메시지는 필수입니다.");
    }

    private String tradePricePayload() {
        return String.join(
                "^",
                "005930",
                "093000",
                "75100",
                "2",
                "100",
                "0.13",
                "75000",
                "75200",
                "74900",
                "75000",
                "0",
                "0",
                "1234567"
        );
    }
}
