package com.tumo.stock.adapter.out.kis.websocket;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tumo.stock.adapter.out.kis.config.KisProperties;
import org.junit.jupiter.api.Test;

class KisWebSocketSubscribeMessageTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void subscribeTradePrice() throws Exception {
        KisWebSocketSubscribeMessage message = KisWebSocketSubscribeMessage.subscribeTradePrice(
                "test-approval-key",
                properties(),
                "005930"
        );

        JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(message));

        assertThat(jsonNode.at("/header/approval_key").asText()).isEqualTo("test-approval-key");
        assertThat(jsonNode.at("/header/custtype").asText()).isEqualTo("P");
        assertThat(jsonNode.at("/header/tr_type").asText()).isEqualTo("1");
        assertThat(jsonNode.at("/header/content-type").asText()).isEqualTo("utf-8");
        assertThat(jsonNode.at("/body/input/tr_id").asText()).isEqualTo("H0STCNT0");
        assertThat(jsonNode.at("/body/input/tr_key").asText()).isEqualTo("005930");
    }

    @Test
    void subscribeOrderBook() throws Exception {
        KisWebSocketSubscribeMessage message = KisWebSocketSubscribeMessage.subscribeOrderBook(
                "test-approval-key",
                properties(),
                "000660"
        );

        JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(message));

        assertThat(jsonNode.at("/header/approval_key").asText()).isEqualTo("test-approval-key");
        assertThat(jsonNode.at("/header/custtype").asText()).isEqualTo("P");
        assertThat(jsonNode.at("/header/tr_type").asText()).isEqualTo("1");
        assertThat(jsonNode.at("/header/content-type").asText()).isEqualTo("utf-8");
        assertThat(jsonNode.at("/body/input/tr_id").asText()).isEqualTo("H0STASP0");
        assertThat(jsonNode.at("/body/input/tr_key").asText()).isEqualTo("000660");
    }

    private KisProperties properties() {
        return new KisProperties(
                true,
                "test-app-key",
                "test-app-secret",
                "https://kis.example.com",
                "ws://kis.example.com",
                "/tryitout",
                "P",
                "H0STCNT0",
                "H0STASP0"
        );
    }
}
