package com.tumo.stock.adapter.out.kis.config;

import com.tumo.stock.adapter.out.kis.auth.KisApprovalKeyClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * KIS adapter bean 설정.
 */
@Configuration
public class KisConfiguration {

    /**
     * KIS WebSocket approval key 발급 client bean을 생성한다.
     *
     * @param restClientBuilder Spring RestClient builder
     * @param properties KIS Open API 연동 설정 값
     * @return KIS approval key 발급 client
     */
    @Bean
    @ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "true")
    KisApprovalKeyClient kisApprovalKeyClient(
            RestClient.Builder restClientBuilder,
            KisProperties properties
    ) {
        return new KisApprovalKeyClient(restClientBuilder, properties);
    }
}
