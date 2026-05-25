package com.tumo.stock.adapter.out.kis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * KIS Open API 연동에 필요한 설정 값.
 *
 * @param enabled KIS adapter 활성화 여부
 * @param appKey KIS Developers에서 발급받은 app key
 * @param appSecret KIS Developers에서 발급받은 app secret
 * @param restUrl KIS REST API 기본 URL
 * @param websocketUrl KIS WebSocket 기본 URL
 * @param websocketPath KIS WebSocket 접속 경로
 * @param customerType KIS 고객 타입
 * @param tradePriceTrId 국내주식 실시간체결가 TR ID
 * @param orderBookTrId 국내주식 실시간호가 TR ID
 */
@ConfigurationProperties(prefix = "kis")
public record KisProperties(
        boolean enabled,
        String appKey,
        String appSecret,
        String restUrl,
        String websocketUrl,
        String websocketPath,
        String customerType,
        String tradePriceTrId,
        String orderBookTrId
) {
}
