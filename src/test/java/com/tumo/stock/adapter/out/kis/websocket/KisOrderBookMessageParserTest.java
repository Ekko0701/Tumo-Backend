package com.tumo.stock.adapter.out.kis.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tumo.stock.domain.orderbook.StockOrderBookEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class KisOrderBookMessageParserTest {

    private final KisOrderBookMessageParser parser = new KisOrderBookMessageParser("H0STASP0");

    @Test
    void parseOrderBookMessage() {
        String rawMessage = "0|H0STASP0|001|" + orderBookPayload();

        StockOrderBookEvent event = parser.parse(rawMessage);

        assertThat(event.provider()).isEqualTo("KIS");
        assertThat(event.orderBook().stockCode()).isEqualTo("005930");
        assertThat(event.orderBook().askLevels()).hasSize(10);
        assertThat(event.orderBook().bidLevels()).hasSize(10);
        assertThat(event.orderBook().bestAsk().price()).isEqualTo(75200L);
        assertThat(event.orderBook().bestAsk().volume()).isEqualTo(3000L);
        assertThat(event.orderBook().bestBid().price()).isEqualTo(75100L);
        assertThat(event.orderBook().bestBid().volume()).isEqualTo(2500L);
        assertThat(event.orderBook().spread()).isEqualTo(100L);
        assertThat(event.orderBook().orderBookChangedAt().getHour()).isEqualTo(9);
        assertThat(event.orderBook().orderBookChangedAt().getMinute()).isEqualTo(30);
    }

    @Test
    void throwsExceptionWhenTrIdDoesNotMatch() {
        String rawMessage = "0|H0STCNT0|001|" + orderBookPayload();

        assertThatThrownBy(() -> parser.parse(rawMessage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("KIS 호가 TR ID가 일치하지 않습니다.");
    }

    @Test
    void throwsExceptionWhenFieldCountIsNotEnough() {
        String rawMessage = "0|H0STASP0|001|005930^093000^0";

        assertThatThrownBy(() -> parser.parse(rawMessage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("KIS 호가 payload 필드 수가 부족합니다.");
    }

    @Test
    void throwsExceptionWhenRawMessageIsBlank() {
        assertThatThrownBy(() -> parser.parse(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("KIS 호가 메시지는 필수입니다.");
    }

    private String orderBookPayload() {
        List<String> fields = new ArrayList<>();

        fields.add("005930");
        fields.add("093000");
        fields.add("0");

        for (int index = 0; index < 10; index++) {
            fields.add(String.valueOf(75200 + index * 100));
        }

        for (int index = 0; index < 10; index++) {
            fields.add(String.valueOf(75100 - index * 100));
        }

        for (int index = 0; index < 10; index++) {
            fields.add(String.valueOf(3000 + index));
        }

        for (int index = 0; index < 10; index++) {
            fields.add(String.valueOf(2500 + index));
        }

        return String.join("^", fields);
    }
}
