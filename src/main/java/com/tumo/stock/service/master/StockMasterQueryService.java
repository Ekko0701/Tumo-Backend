package com.tumo.stock.service.master;

import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.dto.StockPageResponse;
import com.tumo.stock.dto.StockResponse;
import com.tumo.stock.repository.StockRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DB에 저장된 종목 마스터 정보를 조회하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockMasterQueryService {

    private final StockRepository stockRepository;

    /**
     * 시장별 종목 마스터를 page 단위로 조회한다.
     *
     * @param market 조회할 시장 구분
     * @param page 조회할 page 번호
     * @param size 조회할 page 크기
     * @return DB에 저장된 종목 마스터 page
     */
    public StockPageResponse getStockMasters(Market market, int page, int size) {
        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by("stockName").ascending()
                        .and(Sort.by("stockCode").ascending())
        );
        Page<Stock> stockPage = stockRepository.findByMarket(market, pageRequest);
        List<StockResponse> stocks = stockPage.getContent().stream()
                .map(StockResponse::from)
                .toList();

        return new StockPageResponse(
                stocks,
                stockPage.getNumber(),
                stockPage.getSize(),
                stockPage.hasNext()
        );
    }
}
