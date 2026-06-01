package com.tumo.stock.adapter.out.kis.rest.quotation;

import com.tumo.stock.adapter.out.kis.rest.client.KisRestClient;
import com.tumo.stock.domain.price.StockPrice;
import com.tumo.stock.port.query.StockPriceQueryPort;
import java.util.Objects;
import java.util.Optional;

/**
 * KIS REST API로 종목 현재가를 조회하는 outbound adapter.
 */
public class KisStockPriceQueryClient implements StockPriceQueryPort {

    /**
     * KIS REST API 공통 client.
     */
    private final KisRestClient restClient;

    /**
     * KIS 종목 현재가 조회 adapter를 생성한다.
     *
     * @param restClient KIS REST API 공통 client
     */
    public KisStockPriceQueryClient(KisRestClient restClient) {
        this.restClient = Objects.requireNonNull(restClient, "KIS REST client는 필수입니다.");
    }

    /**
     * KIS REST API로 종목 현재가를 조회한다.
     *
     * @param stockCode 최신 가격을 조회할 종목 코드
     * @return KIS에서 조회한 종목 현재가
     */
    @Override
    public Optional<StockPrice> findCurrentPrice(String stockCode) {
        KisInquirePriceRequest request = new KisInquirePriceRequest(stockCode);
        KisInquirePriceResponse response = restClient.get(request.toKisRestRequest());

        return Optional.of(response.toStockPrice(stockCode));
    }
}
