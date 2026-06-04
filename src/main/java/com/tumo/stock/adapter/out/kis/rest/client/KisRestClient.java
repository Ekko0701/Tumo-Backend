package com.tumo.stock.adapter.out.kis.rest.client;

import com.tumo.stock.adapter.out.kis.rest.auth.KisAccessTokenClient;
import com.tumo.stock.adapter.out.kis.config.KisProperties;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

/**
 * KIS REST API 호출에 필요한 공통 인증 header와 요청 실행을 담당하는 client.
 */
public class KisRestClient {

    /**
     * KIS REST API app key header 이름.
     */
    private static final String APP_KEY_HEADER = "appkey";

    /**
     * KIS REST API app secret header 이름.
     */
    private static final String APP_SECRET_HEADER = "appsecret";

    /**
     * KIS REST API transaction id header 이름.
     */
    private static final String TRANSACTION_ID_HEADER = "tr_id";

    /**
     * KIS REST API 고객 타입 header 이름.
     */
    private static final String CUSTOMER_TYPE_HEADER = "custtype";

    /**
     * Bearer 인증 scheme.
     */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * KIS REST API를 호출하는 Spring HTTP client.
     */
    private final RestClient restClient;

    /**
     * KIS REST API access token 발급 client.
     */
    private final KisAccessTokenClient accessTokenClient;

    /**
     * KIS Open API 연동에 필요한 설정 값.
     */
    private final KisProperties properties;

    /**
     * KIS REST 공통 client를 생성한다.
     *
     * @param restClientBuilder Spring RestClient builder
     * @param accessTokenClient KIS REST access token 발급 client
     * @param properties KIS Open API 연동 설정 값
     */
    public KisRestClient(
            RestClient.Builder restClientBuilder,
            KisAccessTokenClient accessTokenClient,
            KisProperties properties
    ) {
        this.restClient = restClientBuilder
                .baseUrl(properties.restUrl())
                .build();
        this.accessTokenClient = Objects.requireNonNull(accessTokenClient, "KIS access token client는 필수입니다.");
        this.properties = Objects.requireNonNull(properties, "KIS 설정 값은 필수입니다.");
    }

    /**
     * KIS REST GET API를 호출한다.
     *
     * @param request KIS REST GET API 요청 값
     * @return 변환된 응답 body
     * @param <T> 응답 body 타입
     */
    public <T> T get(KisRestRequest<T> request) {
        Objects.requireNonNull(request, "KIS REST API 요청 값은 필수입니다.");

        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(request.path());
                    request.queryParameters().forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .headers(headers -> applyKisHeaders(headers, request.transactionId()))
                .retrieve()
                .body(request.responseType());
    }

    private void applyKisHeaders(HttpHeaders headers, String transactionId) {
        headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessTokenClient.getAccessToken());
        headers.set(APP_KEY_HEADER, properties.appKey());
        headers.set(APP_SECRET_HEADER, properties.appSecret());
        headers.set(TRANSACTION_ID_HEADER, transactionId);
        headers.set(CUSTOMER_TYPE_HEADER, properties.customerType());
    }
}
