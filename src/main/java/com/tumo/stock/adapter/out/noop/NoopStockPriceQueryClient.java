package com.tumo.stock.adapter.out.noop;

import com.tumo.stock.domain.price.StockPrice;
import com.tumo.stock.port.query.StockPriceQueryPort;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 실제 현재가 조회 provider가 설정되지 않았을 때 빈 결과를 반환하는 기본 query adapter.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoopStockPriceQueryClient implements StockPriceQueryPort {

    /**
     * 외부 provider를 호출하지 않고 현재가 조회 불가 상태를 반환한다.
     *
     * @param stockCode 최신 가격을 조회할 종목 코드
     * @return 빈 현재가 조회 결과
     */
    @Override
    public Optional<StockPrice> findCurrentPrice(String stockCode) {
        log.debug("Stock price query client is not configured yet. stockCode={}", stockCode);
        return Optional.empty();
    }
}
