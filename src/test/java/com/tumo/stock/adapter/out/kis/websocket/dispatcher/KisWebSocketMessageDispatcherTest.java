package com.tumo.stock.adapter.out.kis.websocket.dispatcher;

import static org.assertj.core.api.Assertions.assertThat;

import com.tumo.stock.adapter.out.kis.config.KisProperties;
import com.tumo.stock.adapter.out.kis.websocket.parser.KisOrderBookMessageParser;
import com.tumo.stock.adapter.out.kis.websocket.parser.KisTradePriceMessageParser;
import com.tumo.stock.domain.orderbook.StockOrderBookEvent;
import com.tumo.stock.domain.price.StockPriceEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class KisWebSocketMessageDispatcherTest {

    private final KisProperties properties = new KisProperties(
            true,
            "app-key",
            "app-secret",
            "https://openapi.koreainvestment.com:9443",
            "ws://ops.koreainvestment.com:21000",
            "/tryitout/H0STCNT0",
            "P",
            "H0STCNT0",
            "H0STASP0"
    );

    private final KisWebSocketMessageDispatcher dispatcher = new KisWebSocketMessageDispatcher(
            properties,
            new KisTradePriceMessageParser(properties.tradePriceTrId()),
            new KisOrderBookMessageParser(properties.orderBookTrId())
    );

    @Test
    void dispatchTradePriceMessage() {
        AtomicReference<StockPriceEvent> handledEvent = new AtomicReference<>();
        String rawMessage = "0|H0STCNT0|001|" + tradePricePayload();

        dispatcher.dispatch(rawMessage, handledEvent::set, null);

        assertThat(handledEvent.get()).isNotNull();
        assertThat(handledEvent.get().price().stockCode()).isEqualTo("005930");
        assertThat(handledEvent.get().price().currentPrice()).isEqualTo(75100L);
    }

    @Test
    void dispatchOrderBookMessage() {
        AtomicReference<StockOrderBookEvent> handledEvent = new AtomicReference<>();
        String rawMessage = "0|H0STASP0|001|" + orderBookPayload();

        dispatcher.dispatch(rawMessage, null, handledEvent::set);

        assertThat(handledEvent.get()).isNotNull();
        assertThat(handledEvent.get().orderBook().stockCode()).isEqualTo("005930");
        assertThat(handledEvent.get().orderBook().bestAsk().price()).isEqualTo(75200L);
        assertThat(handledEvent.get().orderBook().bestBid().price()).isEqualTo(75100L);
    }

    @Test
    void ignoreControlMessage() {
        AtomicReference<StockPriceEvent> priceEvent = new AtomicReference<>();
        AtomicReference<StockOrderBookEvent> orderBookEvent = new AtomicReference<>();

        dispatcher.dispatch("{\"header\":{\"tr_id\":\"PINGPONG\"}}", priceEvent::set, orderBookEvent::set);

        assertThat(priceEvent.get()).isNull();
        assertThat(orderBookEvent.get()).isNull();
    }

    @Test
    void ignoreUnknownMessage() {
        AtomicReference<StockPriceEvent> priceEvent = new AtomicReference<>();
        AtomicReference<StockOrderBookEvent> orderBookEvent = new AtomicReference<>();

        dispatcher.dispatch("0|UNKNOWN|001|005930^093000", priceEvent::set, orderBookEvent::set);

        assertThat(priceEvent.get()).isNull();
        assertThat(orderBookEvent.get()).isNull();
    }

    @Test
    void ignoreWhenHandlerIsNull() {
        dispatcher.dispatch("0|H0STCNT0|001|" + tradePricePayload(), null, null);
        dispatcher.dispatch("0|H0STASP0|001|" + orderBookPayload(), null, null);
    }

    @Test
    void ignoreParseFailure() {
        AtomicReference<StockPriceEvent> priceEvent = new AtomicReference<>();

        dispatcher.dispatch("0|H0STCNT0|001|005930^093000^75100", priceEvent::set, null);

        assertThat(priceEvent.get()).isNull();
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
