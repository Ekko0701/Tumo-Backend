package com.tumo.stock.adapter.out.noop;

import com.tumo.stock.domain.ranking.StockRanking;
import com.tumo.stock.domain.ranking.StockRankingType;
import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.port.query.StockRankingQueryPort;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 실제 랭킹 조회 provider가 설정되지 않았을 때 빈 결과를 반환하는 기본 query adapter.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoopStockRankingQueryClient implements StockRankingQueryPort {

    /**
     * 외부 provider를 호출하지 않고 빈 랭킹 목록을 반환한다.
     *
     * @param market 랭킹을 조회할 시장
     * @param type 랭킹 기준
     * @return 빈 랭킹 목록
     */
    @Override
    public List<StockRanking> findRankings(Market market, StockRankingType type) {
        log.debug("Stock ranking query client is not configured yet. market={}, type={}", market, type);
        return List.of();
    }
}
