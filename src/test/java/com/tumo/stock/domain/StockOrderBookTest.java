package com.tumo.stock.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class StockOrderBookTest {

    @Test
    void createStockOrderBook() {
        LocalDateTime orderBookChangedAt = LocalDateTime.of(2026, 5, 25, 9, 1);
        StockOrderBookLevel askLevel = new StockOrderBookLevel(75200L, 3000L);
        StockOrderBookLevel bidLevel = new StockOrderBookLevel(75100L, 2500L);

        StockOrderBook orderBook = new StockOrderBook(
                "005930",
                List.of(askLevel),
                List.of(bidLevel),
                orderBookChangedAt
        );

        assertThat(orderBook.stockCode()).isEqualTo("005930");
        assertThat(orderBook.askLevels()).containsExactly(askLevel);
        assertThat(orderBook.bidLevels()).containsExactly(bidLevel);
        assertThat(orderBook.orderBookChangedAt()).isEqualTo(orderBookChangedAt);
    }

    @Test
    void throwsExceptionWhenStockCodeIsBlank() {
        assertThatThrownBy(() -> new StockOrderBook(
                " ",
                List.of(new StockOrderBookLevel(75200L, 3000L)),
                List.of(new StockOrderBookLevel(75100L, 2500L)),
                LocalDateTime.of(2026, 5, 25, 9, 1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종목 코드는 필수입니다.");
    }

    @Test
    void throwsExceptionWhenAskLevelsIsNull() {
        assertThatThrownBy(() -> new StockOrderBook(
                "005930",
                null,
                List.of(new StockOrderBookLevel(75100L, 2500L)),
                LocalDateTime.of(2026, 5, 25, 9, 1)
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("매도 호가 목록은 필수입니다.");
    }

    @Test
    void throwsExceptionWhenBidLevelsIsNull() {
        assertThatThrownBy(() -> new StockOrderBook(
                "005930",
                List.of(new StockOrderBookLevel(75200L, 3000L)),
                null,
                LocalDateTime.of(2026, 5, 25, 9, 1)
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("매수 호가 목록은 필수입니다.");
    }

    @Test
    void throwsExceptionWhenOrderBookChangedAtIsNull() {
        assertThatThrownBy(() -> new StockOrderBook(
                "005930",
                List.of(new StockOrderBookLevel(75200L, 3000L)),
                List.of(new StockOrderBookLevel(75100L, 2500L)),
                null
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("호가 변경 시각은 필수입니다.");
    }

    @Test
    void bestAskReturnsFirstAskLevel() {
        StockOrderBookLevel bestAsk = new StockOrderBookLevel(75200L, 3000L);
        StockOrderBook orderBook = createOrderBook(
                List.of(bestAsk, new StockOrderBookLevel(75300L, 2000L)),
                List.of(new StockOrderBookLevel(75100L, 2500L))
        );

        assertThat(orderBook.bestAsk()).isEqualTo(bestAsk);
    }

    @Test
    void bestAskReturnsNullWhenAskLevelsIsEmpty() {
        StockOrderBook orderBook = createOrderBook(
                List.of(),
                List.of(new StockOrderBookLevel(75100L, 2500L))
        );

        assertThat(orderBook.bestAsk()).isNull();
    }

    @Test
    void bestBidReturnsFirstBidLevel() {
        StockOrderBookLevel bestBid = new StockOrderBookLevel(75100L, 2500L);
        StockOrderBook orderBook = createOrderBook(
                List.of(new StockOrderBookLevel(75200L, 3000L)),
                List.of(bestBid, new StockOrderBookLevel(75000L, 2000L))
        );

        assertThat(orderBook.bestBid()).isEqualTo(bestBid);
    }

    @Test
    void bestBidReturnsNullWhenBidLevelsIsEmpty() {
        StockOrderBook orderBook = createOrderBook(
                List.of(new StockOrderBookLevel(75200L, 3000L)),
                List.of()
        );

        assertThat(orderBook.bestBid()).isNull();
    }

    @Test
    void spreadReturnsDifferenceBetweenBestAskAndBestBid() {
        StockOrderBook orderBook = createOrderBook(
                List.of(new StockOrderBookLevel(75200L, 3000L)),
                List.of(new StockOrderBookLevel(75100L, 2500L))
        );

        assertThat(orderBook.spread()).isEqualTo(100L);
    }

    @Test
    void spreadReturnsNullWhenBestAskDoesNotExist() {
        StockOrderBook orderBook = createOrderBook(
                List.of(),
                List.of(new StockOrderBookLevel(75100L, 2500L))
        );

        assertThat(orderBook.spread()).isNull();
    }

    @Test
    void spreadReturnsNullWhenBestBidDoesNotExist() {
        StockOrderBook orderBook = createOrderBook(
                List.of(new StockOrderBookLevel(75200L, 3000L)),
                List.of()
        );

        assertThat(orderBook.spread()).isNull();
    }

    @Test
    void stockOrderBookCopiesLevels() {
        List<StockOrderBookLevel> askLevels = new ArrayList<>();
        List<StockOrderBookLevel> bidLevels = new ArrayList<>();
        askLevels.add(new StockOrderBookLevel(75200L, 3000L));
        bidLevels.add(new StockOrderBookLevel(75100L, 2500L));

        StockOrderBook orderBook = createOrderBook(askLevels, bidLevels);

        askLevels.add(new StockOrderBookLevel(75300L, 2000L));
        bidLevels.add(new StockOrderBookLevel(75000L, 2000L));

        assertThat(orderBook.askLevels()).hasSize(1);
        assertThat(orderBook.bidLevels()).hasSize(1);
    }

    private StockOrderBook createOrderBook(
            List<StockOrderBookLevel> askLevels,
            List<StockOrderBookLevel> bidLevels
    ) {
        return new StockOrderBook(
                "005930",
                askLevels,
                bidLevels,
                LocalDateTime.of(2026, 5, 25, 9, 1)
        );
    }
}
