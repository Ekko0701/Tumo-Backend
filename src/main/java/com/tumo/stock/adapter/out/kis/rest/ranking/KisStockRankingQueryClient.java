package com.tumo.stock.adapter.out.kis.rest.ranking;

import com.tumo.stock.adapter.out.kis.rest.client.KisRestClient;
import com.tumo.stock.domain.ranking.StockRanking;
import com.tumo.stock.domain.ranking.StockRankingType;
import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.port.query.StockRankingQueryPort;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * KIS REST API로 종목 랭킹 목록을 조회하는 outbound adapter.
 */
public class KisStockRankingQueryClient implements StockRankingQueryPort {

    private final KisRestClient restClient;

    /**
     * KIS 종목 랭킹 조회 adapter를 생성한다.
     *
     * @param restClient KIS REST API 공통 client
     */
    public KisStockRankingQueryClient(KisRestClient restClient) {
        this.restClient = Objects.requireNonNull(restClient, "KIS REST client는 필수입니다.");
    }

    /**
     * KIS REST API로 종목 랭킹 목록을 조회한다.
     *
     * @param market 랭킹을 조회할 시장
     * @param type 랭킹 기준
     * @return KIS에서 조회한 종목 랭킹 목록
     */
    @Override
    public List<StockRanking> findRankings(Market market, StockRankingType type) {
        return switch (type) {
            case TRADE_AMOUNT, TRADE_VOLUME -> findVolumeRankings(market, type);
            case RISING, FALLING -> findFluctuationRankings(market, type);
            case POPULAR -> List.of();
        };
    }

    private List<StockRanking> findVolumeRankings(Market market, StockRankingType type) {
        KisVolumeRankRequest request = new KisVolumeRankRequest(market, type);
        KisStockRankingResponse response = restClient.get(request.toKisRestRequest());

        return response.toStockRankings(market);
    }

    private List<StockRanking> findFluctuationRankings(Market market, StockRankingType type) {
        KisFluctuationRankRequest request = new KisFluctuationRankRequest(market, type);
        KisStockRankingResponse response = restClient.get(request.toKisRestRequest());

        Comparator<StockRanking> comparator = switch (type) {
            case RISING -> Comparator.comparing(StockRanking::changeRate).reversed();
            case FALLING -> Comparator.comparing(StockRanking::changeRate);
            case TRADE_AMOUNT, TRADE_VOLUME, POPULAR -> throw new IllegalArgumentException("등락률 순위 요청 타입이 아닙니다.");
        };

        return response.toStockRankings(market)
                .stream()
                .sorted(comparator)
                .toList();
    }
}
