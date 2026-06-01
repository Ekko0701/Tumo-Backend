package com.tumo.stock.service.master;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.dto.StockPageResponse;
import com.tumo.stock.dto.StockResponse;
import com.tumo.stock.repository.StockRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class StockMasterQueryServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockMasterQueryService stockMasterQueryService;

    @Test
    void getStockMasters() {
        LocalDateTime priceChangedAt = LocalDateTime.of(2026, 6, 1, 10, 0);
        Stock samsung = new Stock("005930", "삼성전자", Market.KOSPI, 80000L, priceChangedAt);
        given(stockRepository.findByMarket(eq(Market.KOSPI), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(samsung), PageRequest.of(0, 30), 31));

        StockPageResponse response = stockMasterQueryService.getStockMasters(Market.KOSPI, 0, 30);

        assertThat(response.stocks()).hasSize(1);
        assertThat(response.stocks())
                .extracting(StockResponse::stockCode)
                .containsExactly("005930");
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(30);
        assertThat(response.hasNext()).isTrue();
    }

    @Test
    void getStockMastersUsesStockNameAndStockCodeSort() {
        given(stockRepository.findByMarket(eq(Market.KOSPI), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), PageRequest.of(1, 20), 0));
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        stockMasterQueryService.getStockMasters(Market.KOSPI, 1, 20);

        verify(stockRepository).findByMarket(eq(Market.KOSPI), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("stockName")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("stockName").isAscending()).isTrue();
        assertThat(pageable.getSort().getOrderFor("stockCode")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("stockCode").isAscending()).isTrue();
    }
}
