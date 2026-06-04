package com.tumo.stock.port.query;

import com.tumo.stock.domain.ranking.StockRanking;
import com.tumo.stock.domain.ranking.StockRankingType;
import com.tumo.stock.domain.stock.Market;
import java.util.List;

/**
 * 종목 랭킹 목록을 조회하는 outbound port.
 */
public interface StockRankingQueryPort {

    /**
     * 시장과 랭킹 기준에 해당하는 종목 랭킹 목록을 조회한다.
     *
     * @param market 랭킹을 조회할 시장
     * @param type 랭킹 기준
     * @return 종목 랭킹 목록
     */
    List<StockRanking> findRankings(Market market, StockRankingType type);
}
