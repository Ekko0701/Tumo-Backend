package com.tumo.stock.adapter.out.kis.rest;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * KIS REST API 호출에 필요한 요청 정보를 표현하는 값 객체.
 *
 * @param path KIS REST API path
 * @param transactionId KIS REST API transaction id
 * @param queryParameters KIS REST API query parameter 목록
 * @param responseType 응답 body를 변환할 타입
 * @param <T> 응답 body 타입
 */
public record KisRestRequest<T>(
        String path,
        String transactionId,
        Map<String, String> queryParameters,
        Class<T> responseType
) {

    /**
     * KIS REST API 요청 값을 생성한다.
     *
     * @param path KIS REST API path
     * @param transactionId KIS REST API transaction id
     * @param queryParameters KIS REST API query parameter 목록
     * @param responseType 응답 body를 변환할 타입
     */
    public KisRestRequest {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("KIS REST API path는 필수입니다.");
        }

        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("KIS REST API transaction id는 필수입니다.");
        }

        queryParameters = queryParameters == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(queryParameters));
        Objects.requireNonNull(responseType, "KIS REST API 응답 타입은 필수입니다.");
    }

    /**
     * KIS REST GET API 요청 값을 생성한다.
     *
     * @param path KIS REST API path
     * @param transactionId KIS REST API transaction id
     * @param queryParameters KIS REST API query parameter 목록
     * @param responseType 응답 body를 변환할 타입
     * @return KIS REST GET API 요청 값
     * @param <T> 응답 body 타입
     */
    public static <T> KisRestRequest<T> get(
            String path,
            String transactionId,
            Map<String, String> queryParameters,
            Class<T> responseType
    ) {
        return new KisRestRequest<>(path, transactionId, queryParameters, responseType);
    }
}
