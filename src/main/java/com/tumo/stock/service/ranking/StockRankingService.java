package com.tumo.stock.service.ranking;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.domain.ranking.StockRankingType;
import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.dto.StockPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 종목 랭킹 목록 조회를 담당하는 서비스.
 */
@Service
@Transactional(readOnly = true)
public class StockRankingService {

    /**
     * 시장과 랭킹 기준에 해당하는 종목 page를 조회한다.
     *
     * @param market 랭킹을 조회할 시장
     * @param type 랭킹 기준
     * @param page 0부터 시작하는 page 번호
     * @param size 한 page에 조회할 종목 수
     * @return 종목 랭킹 page 응답
     */
    public StockPageResponse getRankings(
            Market market,
            StockRankingType type,
            int page,
            int size
    ) {
        throw new BusinessException(ErrorCode.STOCK_RANKING_NOT_SUPPORTED);
    }
}
