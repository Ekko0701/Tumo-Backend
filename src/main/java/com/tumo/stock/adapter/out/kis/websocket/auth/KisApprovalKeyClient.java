package com.tumo.stock.adapter.out.kis.websocket.auth;

import com.tumo.stock.adapter.out.kis.config.KisProperties;
import org.springframework.web.client.RestClient;

/**
 * KIS WebSocket 접속에 필요한 approval key를 발급받는 client.
 */
public class KisApprovalKeyClient {

    /**
     * KIS REST API를 호출하는 Spring HTTP client.
     */
    private final RestClient restClient;

    /**
     * KIS Open API 연동에 필요한 설정 값.
     */
    private final KisProperties properties;

    /**
     * KIS approval key 발급 client를 생성한다.
     *
     * @param restClientBuilder Spring RestClient builder
     * @param properties KIS Open API 연동 설정 값
     */
    public KisApprovalKeyClient(RestClient.Builder restClientBuilder, KisProperties properties) {
        this.restClient = restClientBuilder
                .baseUrl(properties.restUrl())
                .build();
        this.properties = properties;
    }

    /**
     * KIS WebSocket 접속에 사용할 approval key를 발급받는다.
     *
     * @return KIS WebSocket approval key
     */
    public String issueApprovalKey() {
        validateCredentials();

        KisApprovalKeyResponse response = restClient.post()
                .uri("/oauth2/Approval")
                .body(KisApprovalKeyRequest.from(properties))
                .retrieve()
                .body(KisApprovalKeyResponse.class);

        if (response == null || response.approvalKey() == null || response.approvalKey().isBlank()) {
            throw new IllegalStateException("KIS WebSocket approval key 발급 응답이 비어 있습니다.");
        }

        return response.approvalKey();
    }

    private void validateCredentials() {
        if (properties.appKey() == null || properties.appKey().isBlank()) {
            throw new IllegalStateException("KIS app key가 설정되지 않았습니다.");
        }

        if (properties.appSecret() == null || properties.appSecret().isBlank()) {
            throw new IllegalStateException("KIS app secret이 설정되지 않았습니다.");
        }
    }
}
