package com.tumo.stock.service;

import static org.mockito.Mockito.verify;

import com.tumo.stock.domain.orderbook.StockOrderBook;
import com.tumo.stock.domain.orderbook.StockOrderBookEvent;
import com.tumo.stock.domain.orderbook.StockOrderBookLevel;
import com.tumo.stock.port.StockOrderBookPublisher;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockOrderBookServiceTest {

    @Mock
    private StockOrderBookPublisher stockOrderBookPublisher;

    @InjectMocks
    private StockOrderBookService stockOrderBookService;

    @Test
    void handlePublishesOrderBookEvent() {
        StockOrderBookEvent event = StockOrderBookEvent.fromKis(
                new StockOrderBook(
                        "005930",
                        List.of(new StockOrderBookLevel(75200L, 3000L)),
                        List.of(new StockOrderBookLevel(75100L, 2500L)),
                        LocalDateTime.of(2026, 5, 25, 9, 1)
                ),
                LocalDateTime.of(2026, 5, 25, 9, 1, 1)
        );

        stockOrderBookService.handle(event);

        verify(stockOrderBookPublisher).publish(event);
    }
}
