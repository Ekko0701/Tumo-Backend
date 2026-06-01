package com.tumo.stock.adapter.out.kis.rest.auth;

import com.tumo.stock.adapter.out.kis.config.KisProperties;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.springframework.web.client.RestClient;

/**
 * KIS REST API 호출에 필요한 access token을 발급하고 재사용하는 client.
 */
public class KisAccessTokenClient {

    /**
     * KIS access token 발급 응답의 만료 시각 포맷.
     */
    private static final DateTimeFormatter ACCESS_TOKEN_EXPIRED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 만료 직전 token 사용을 피하기 위한 여유 시간.
     */
    private static final Duration EXPIRATION_SAFETY_MARGIN = Duration.ofMinutes(1);

    /**
     * KIS REST API를 호출하는 Spring HTTP client.
     */
    private final RestClient restClient;

    /**
     * KIS Open API 연동에 필요한 설정 값.
     */
    private final KisProperties properties;

    /**
     * 현재 메모리에 보관 중인 KIS REST access token.
     */
    private volatile CachedAccessToken cachedAccessToken;

    /**
     * KIS access token 발급 client를 생성한다.
     *
     * @param restClientBuilder Spring RestClient builder
     * @param properties KIS Open API 연동 설정 값
     */
    public KisAccessTokenClient(RestClient.Builder restClientBuilder, KisProperties properties) {
        this.restClient = restClientBuilder
                .baseUrl(properties.restUrl())
                .build();
        this.properties = Objects.requireNonNull(properties, "KIS 설정 값은 필수입니다.");
    }

    /**
     * KIS REST API 호출에 사용할 access token을 반환한다.
     *
     * <p>기존 token이 만료되지 않았다면 메모리에 보관된 token을 재사용하고,
     * token이 없거나 만료 시간이 가까워졌다면 KIS에 새 access token을 요청한다.</p>
     *
     * @return KIS REST API access token
     */
    public synchronized String getAccessToken() {
        validateCredentials();

        if (cachedAccessToken != null && cachedAccessToken.isValid(LocalDateTime.now())) {
            return cachedAccessToken.accessToken();
        }

        KisAccessTokenResponse response = restClient.post()
                .uri("/oauth2/tokenP")
                .body(KisAccessTokenRequest.from(properties))
                .retrieve()
                .body(KisAccessTokenResponse.class);

        if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
            throw new IllegalStateException("KIS REST access token 발급 응답이 비어 있습니다.");
        }

        cachedAccessToken = new CachedAccessToken(response.accessToken(), resolveExpiredAt(response));
        return cachedAccessToken.accessToken();
    }

    private LocalDateTime resolveExpiredAt(KisAccessTokenResponse response) {
        if (response.accessTokenTokenExpired() != null && !response.accessTokenTokenExpired().isBlank()) {
            return LocalDateTime.parse(response.accessTokenTokenExpired(), ACCESS_TOKEN_EXPIRED_AT_FORMATTER);
        }

        if (response.expiresIn() != null && response.expiresIn() > 0) {
            return LocalDateTime.now().plusSeconds(response.expiresIn());
        }

        throw new IllegalStateException("KIS REST access token 만료 시간이 설정되지 않았습니다.");
    }

    private void validateCredentials() {
        if (properties.appKey() == null || properties.appKey().isBlank()) {
            throw new IllegalStateException("KIS app key가 설정되지 않았습니다.");
        }

        if (properties.appSecret() == null || properties.appSecret().isBlank()) {
            throw new IllegalStateException("KIS app secret이 설정되지 않았습니다.");
        }
    }

    /**
     * 메모리에 보관 중인 KIS REST access token과 만료 시각.
     *
     * @param accessToken KIS REST API 호출에 사용할 access token
     * @param expiredAt access token 만료 시각
     */
    private record CachedAccessToken(
            String accessToken,
            LocalDateTime expiredAt
    ) {

        private boolean isValid(LocalDateTime now) {
            return expiredAt.minus(EXPIRATION_SAFETY_MARGIN).isAfter(now);
        }
    }
}
