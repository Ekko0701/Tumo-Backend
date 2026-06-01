package com.tumo.stock.service.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.domain.price.StockPrice;
import com.tumo.stock.domain.price.StockPriceEvent;
import com.tumo.stock.port.publisher.StockPricePublisher;
import com.tumo.stock.repository.StockRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockRealtimePriceServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockPricePublisher stockPricePublisher;

    @InjectMocks
    private StockRealtimePriceService stockRealtimePriceService;

    @Test
    void handleUpdatesStockPriceAndPublishesEvent() {
        LocalDateTime oldChangedAt = LocalDateTime.of(2026, 5, 25, 9, 0);
        LocalDateTime newChangedAt = LocalDateTime.of(2026, 5, 25, 9, 1);
        Stock stock = new Stock("005930", "삼성전자", Market.KOSPI, 75000L, oldChangedAt);
        StockPriceEvent event = StockPriceEvent.fromKis(
                new StockPrice(
                        "005930",
                        75100L,
                        100L,
                        BigDecimal.valueOf(0.13),
                        1234567L,
                        newChangedAt
                ),
                LocalDateTime.of(2026, 5, 25, 9, 1, 1)
        );
        given(stockRepository.findByStockCode("005930")).willReturn(Optional.of(stock));

        stockRealtimePriceService.handle(event);

        assertThat(stock.getCurrentPrice()).isEqualTo(75100L);
        assertThat(stock.getPriceChangedAt()).isEqualTo(newChangedAt);
        verify(stockPricePublisher).publish(event);
    }

    @Test
    void handleThrowsExceptionWhenStockDoesNotExist() {
        StockPriceEvent event = StockPriceEvent.fromKis(
                new StockPrice(
                        "999999",
                        1000L,
                        0L,
                        BigDecimal.ZERO,
                        0L,
                        LocalDateTime.of(2026, 5, 25, 9, 1)
                ),
                LocalDateTime.of(2026, 5, 25, 9, 1, 1)
        );
        given(stockRepository.findByStockCode("999999")).willReturn(Optional.empty());

        assertThatThrownBy(() -> stockRealtimePriceService.handle(event))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STOCK_NOT_FOUND)
                );

        verify(stockPricePublisher, never()).publish(event);
    }
}
