package com.tumo.stock.adapter.out.kis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

class KisPropertiesTest {

    @Test
    void bindKisProperties() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource();
        source.put("kis.enabled", "true");
        source.put("kis.app-key", "test-app-key");
        source.put("kis.app-secret", "test-app-secret");
        source.put("kis.rest-url", "https://openapi.koreainvestment.com:9443");
        source.put("kis.websocket-url", "ws://ops.koreainvestment.com:21000");
        source.put("kis.websocket-path", "/tryitout");
        source.put("kis.customer-type", "P");
        source.put("kis.trade-price-tr-id", "H0STCNT0");
        source.put("kis.order-book-tr-id", "H0STASP0");

        KisProperties properties = new Binder(source)
                .bind("kis", KisProperties.class)
                .orElseThrow(() -> new IllegalStateException("KIS 설정 바인딩에 실패했습니다."));

        assertThat(properties.enabled()).isTrue();
        assertThat(properties.appKey()).isEqualTo("test-app-key");
        assertThat(properties.appSecret()).isEqualTo("test-app-secret");
        assertThat(properties.restUrl()).isEqualTo("https://openapi.koreainvestment.com:9443");
        assertThat(properties.websocketUrl()).isEqualTo("ws://ops.koreainvestment.com:21000");
        assertThat(properties.websocketPath()).isEqualTo("/tryitout");
        assertThat(properties.customerType()).isEqualTo("P");
        assertThat(properties.tradePriceTrId()).isEqualTo("H0STCNT0");
        assertThat(properties.orderBookTrId()).isEqualTo("H0STASP0");
    }
}
