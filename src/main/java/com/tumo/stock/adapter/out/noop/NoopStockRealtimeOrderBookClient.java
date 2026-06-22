package com.tumo.stock.adapter.out.noop;

import com.tumo.stock.port.handler.StockOrderBookEventHandler;
import com.tumo.stock.port.client.StockRealtimeOrderBookClient;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 실제 실시간 호가 provider 구현이 붙기 전까지 구독 요청을 로그로만 처리하는 기본 client adapter.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "kis", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoopStockRealtimeOrderBookClient implements StockRealtimeOrderBookClient {

    /**
     * 실시간 호가 이벤트 구독을 외부 provider에 전달하지 않고 로그로만 남긴다.
     *
     * @param stockCodes 실시간 호가 이벤트를 구독할 종목 코드 목록
     * @param handler 수신한 호가 이벤트를 전달할 handler
     */
    @Override
    public void subscribe(Collection<String> stockCodes, StockOrderBookEventHandler handler) {
        log.debug("Stock realtime order book client is not configured yet. stockCodes={}", stockCodes);
    }

    /**
     * 실시간 호가 이벤트 구독 해제를 외부 provider에 전달하지 않고 로그로만 남긴다.
     *
     * @param stockCodes 구독을 해제할 종목 코드 목록
     */
    @Override
    public void unsubscribeOrderBook(Collection<String> stockCodes) {
        log.debug("Stock realtime order book client is not configured yet. stockCodes={}", stockCodes);
    }
}
