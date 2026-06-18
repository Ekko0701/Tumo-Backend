package com.tumo.stock.adapter.out.kis.config;

import com.tumo.stock.adapter.out.kis.rest.auth.KisAccessTokenClient;
import com.tumo.stock.adapter.out.kis.websocket.auth.KisApprovalKeyClient;
import com.tumo.stock.adapter.out.kis.rest.candle.KisStockCandleQueryClient;
import com.tumo.stock.adapter.out.kis.rest.price.KisStockPriceQueryClient;
import com.tumo.stock.adapter.out.kis.rest.ranking.KisStockRankingQueryClient;
import com.tumo.stock.adapter.out.kis.rest.client.KisRestClient;
import com.tumo.stock.adapter.out.kis.websocket.client.KisRealtimeWebSocketClient;
import com.tumo.stock.adapter.out.kis.websocket.dispatcher.KisWebSocketMessageDispatcher;
import com.tumo.stock.adapter.out.kis.websocket.message.KisWebSocketMessageSender;
import com.tumo.stock.adapter.out.kis.websocket.parser.KisOrderBookMessageParser;
import com.tumo.stock.adapter.out.kis.websocket.parser.KisTradePriceMessageParser;
import com.tumo.stock.adapter.out.kis.websocket.session.KisWebSocketSessionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

/**
 * KIS adapter bean 설정.
 */
@Configuration
public class KisConfiguration {

    /**
     * KIS REST access token 발급 client bean을 생성한다.
     *
     * @param restClientBuilder Spring RestClient builder
     * @param properties KIS Open API 연동 설정 값
     * @return KIS REST access token 발급 client
     */
    @Bean
    @ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "true")
    KisAccessTokenClient kisAccessTokenClient(
            RestClient.Builder restClientBuilder,
            KisProperties properties
    ) {
        return new KisAccessTokenClient(restClientBuilder, properties);
    }

    /**
     * KIS REST API 공통 client bean을 생성한다.
     *
     * @param restClientBuilder Spring RestClient builder
     * @param accessTokenClient KIS REST access token 발급 client
     * @param properties KIS Open API 연동 설정 값
     * @return KIS REST API 공통 client
     */
    @Bean
    @ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "true")
    KisRestClient kisRestClient(
            RestClient.Builder restClientBuilder,
            KisAccessTokenClient accessTokenClient,
            KisProperties properties
    ) {
        return new KisRestClient(restClientBuilder, accessTokenClient, properties);
    }

    /**
     * KIS REST API 기반 종목 현재가 조회 client bean을 생성한다.
     *
     * @param kisRestClient KIS REST API 공통 client
     * @return KIS REST API 기반 종목 현재가 조회 client
     */
    @Bean
    @ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "true")
    KisStockPriceQueryClient kisStockPriceQueryClient(KisRestClient kisRestClient) {
        return new KisStockPriceQueryClient(kisRestClient);
    }

    /**
     * KIS REST API 기반 종목 랭킹 조회 client bean을 생성한다.
     *
     * @param kisRestClient KIS REST API 공통 client
     * @return KIS REST API 기반 종목 랭킹 조회 client
     */
    @Bean
    @ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "true")
    KisStockRankingQueryClient kisStockRankingQueryClient(KisRestClient kisRestClient) {
        return new KisStockRankingQueryClient(kisRestClient);
    }

    /**
     * KIS REST API 기반 종목 캔들(차트) 조회 client bean을 생성한다.
     *
     * @param kisRestClient KIS REST API 공통 client
     * @return KIS REST API 기반 종목 캔들 조회 client
     */
    @Bean
    @ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "true")
    KisStockCandleQueryClient kisStockCandleQueryClient(KisRestClient kisRestClient) {
        return new KisStockCandleQueryClient(kisRestClient);
    }

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

    /**
     * KIS WebSocket 연결 manager bean을 생성한다.
     *
     * @param properties KIS Open API 연동 설정 값
     * @return KIS WebSocket 연결 manager
     */
    @Bean
    @ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "true")
    KisWebSocketSessionManager kisWebSocketSessionManager(KisProperties properties) {
        return new KisWebSocketSessionManager(properties);
    }

    /**
     * KIS WebSocket 메시지 sender bean을 생성한다.
     *
     * @param objectMapper JSON 직렬화 mapper
     * @return KIS WebSocket 메시지 sender
     */
    @Bean
    @ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "true")
    KisWebSocketMessageSender kisWebSocketMessageSender(ObjectMapper objectMapper) {
        return new KisWebSocketMessageSender(objectMapper);
    }

    /**
     * KIS 실시간체결가 메시지 parser bean을 생성한다.
     *
     * @param properties KIS Open API 연동 설정 값
     * @return KIS 실시간체결가 메시지 parser
     */
    @Bean
    @ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "true")
    KisTradePriceMessageParser kisTradePriceMessageParser(KisProperties properties) {
        return new KisTradePriceMessageParser(properties.tradePriceTrId());
    }

    /**
     * KIS 실시간호가 메시지 parser bean을 생성한다.
     *
     * @param properties KIS Open API 연동 설정 값
     * @return KIS 실시간호가 메시지 parser
     */
    @Bean
    @ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "true")
    KisOrderBookMessageParser kisOrderBookMessageParser(KisProperties properties) {
        return new KisOrderBookMessageParser(properties.orderBookTrId());
    }

    /**
     * KIS WebSocket 메시지 dispatcher bean을 생성한다.
     *
     * @param properties KIS Open API 연동 설정 값
     * @param tradePriceMessageParser KIS 실시간체결가 메시지 parser
     * @param orderBookMessageParser KIS 실시간호가 메시지 parser
     * @return KIS WebSocket 메시지 dispatcher
     */
    @Bean
    @ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "true")
    KisWebSocketMessageDispatcher kisWebSocketMessageDispatcher(
            KisProperties properties,
            KisTradePriceMessageParser tradePriceMessageParser,
            KisOrderBookMessageParser orderBookMessageParser
    ) {
        return new KisWebSocketMessageDispatcher(properties, tradePriceMessageParser, orderBookMessageParser);
    }

    /**
     * KIS WebSocket 기반 실시간 시세 client bean을 생성한다.
     *
     * @param approvalKeyClient KIS WebSocket approval key 발급 client
     * @param properties KIS Open API 연동 설정 값
     * @param sessionManager KIS WebSocket 연결 manager
     * @param messageSender KIS WebSocket 메시지 sender
     * @param messageDispatcher KIS WebSocket 수신 메시지 dispatcher
     * @return KIS WebSocket 기반 실시간 시세 client
     */
    @Bean
    @ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "true")
    KisRealtimeWebSocketClient kisRealtimeWebSocketClient(
            KisApprovalKeyClient approvalKeyClient,
            KisProperties properties,
            KisWebSocketSessionManager sessionManager,
            KisWebSocketMessageSender messageSender,
            KisWebSocketMessageDispatcher messageDispatcher
    ) {
        return new KisRealtimeWebSocketClient(
                approvalKeyClient,
                properties,
                sessionManager,
                messageSender,
                messageDispatcher
        );
    }
}
