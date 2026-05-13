package com.tumo.stock.config;

import com.tumo.stock.domain.Market;
import com.tumo.stock.domain.Stock;
import com.tumo.stock.repository.StockRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StockSeedData implements ApplicationRunner {

    private final StockRepository stockRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (stockRepository.count() > 0) {
            return;
        }

        LocalDateTime priceChangedAt = LocalDateTime.now();
        List<Stock> stocks = List.of(
                new Stock("005930", "삼성전자", Market.KOSPI, 75000L, priceChangedAt),
                new Stock("000660", "SK하이닉스", Market.KOSPI, 180000L, priceChangedAt),
                new Stock("035420", "NAVER", Market.KOSPI, 190000L, priceChangedAt),
                new Stock("035720", "카카오", Market.KOSPI, 55000L, priceChangedAt),
                new Stock("247540", "에코프로비엠", Market.KOSDAQ, 210000L, priceChangedAt)
        );

        stockRepository.saveAll(stocks);
    }
}
