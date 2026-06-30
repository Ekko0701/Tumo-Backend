package com.tumo.stock.adapter.out.kis.websocket.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tumo.stock.adapter.out.kis.websocket.auth.KisApprovalKeyClient;
import com.tumo.stock.adapter.out.kis.config.KisProperties;
import com.tumo.stock.adapter.out.kis.websocket.dispatcher.KisWebSocketMessageDispatcher;
import com.tumo.stock.adapter.out.kis.websocket.message.KisWebSocketMessageSender;
import com.tumo.stock.adapter.out.kis.websocket.parser.KisOrderBookMessageParser;
import com.tumo.stock.adapter.out.kis.websocket.parser.KisTradePriceMessageParser;
import com.tumo.stock.adapter.out.kis.websocket.session.KisWebSocketSessionManager;
import com.tumo.stock.domain.orderbook.StockOrderBookEvent;
import com.tumo.stock.domain.price.StockPriceEvent;
import com.tumo.stock.port.handler.StockOrderBookEventHandler;
import com.tumo.stock.port.handler.StockPriceEventHandler;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class KisRealtimeWebSocketClientTest {

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

    private final KisApprovalKeyClient approvalKeyClient = mock(KisApprovalKeyClient.class);
    private final KisWebSocketSessionManager sessionManager = mock(KisWebSocketSessionManager.class);
    private final KisWebSocketMessageSender messageSender = mock(KisWebSocketMessageSender.class);
    private final WebSocket webSocket = mock(WebSocket.class);

    private final KisRealtimeWebSocketClient client = new KisRealtimeWebSocketClient(
            approvalKeyClient,
            properties,
            sessionManager,
            messageSender,
            new KisWebSocketMessageDispatcher(
                    properties,
                    new KisTradePriceMessageParser(properties.tradePriceTrId()),
                    new KisOrderBookMessageParser(properties.orderBookTrId())
            )
    );

    @Test
    void dispatchReceivedTradePriceMessageToSubscribedHandler() {
        AtomicReference<StockPriceEvent> handledEvent = new AtomicReference<>();
        when(approvalKeyClient.issueApprovalKey()).thenReturn("approval-key");
        when(sessionManager.connect(anyMessageHandler())).thenReturn(webSocket);

        client.subscribe(List.of("005930"), (StockPriceEventHandler) handledEvent::set);
        client.handleRawMessage("0|H0STCNT0|001|" + tradePricePayload());

        assertThat(handledEvent.get()).isNotNull();
        assertThat(handledEvent.get().price().stockCode()).isEqualTo("005930");
        assertThat(handledEvent.get().price().currentPrice()).isEqualTo(75100L);
        verify(sessionManager).connect(anyMessageHandler());
    }

    @Test
    void dispatchReceivedOrderBookMessageToSubscribedHandler() {
        AtomicReference<StockOrderBookEvent> handledEvent = new AtomicReference<>();
        when(approvalKeyClient.issueApprovalKey()).thenReturn("approval-key");
        when(sessionManager.connect(anyMessageHandler())).thenReturn(webSocket);

        client.subscribe(List.of("005930"), (StockOrderBookEventHandler) handledEvent::set);
        client.handleRawMessage("0|H0STASP0|001|" + orderBookPayload());

        assertThat(handledEvent.get()).isNotNull();
        assertThat(handledEvent.get().orderBook().stockCode()).isEqualTo("005930");
        assertThat(handledEvent.get().orderBook().bestAsk().price()).isEqualTo(75200L);
        assertThat(handledEvent.get().orderBook().bestBid().price()).isEqualTo(75100L);
        verify(sessionManager).connect(anyMessageHandler());
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
                "50",
                "1234567",
                "92592592500"
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

    private Consumer<String> anyMessageHandler() {
        return any();
    }
}
