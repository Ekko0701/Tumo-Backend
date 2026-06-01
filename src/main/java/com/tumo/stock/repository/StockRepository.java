package com.tumo.stock.repository;

import com.tumo.stock.domain.stock.Stock;
import com.tumo.stock.domain.stock.Market;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Page<Stock> findByMarket(Market market, Pageable pageable);

    Optional<Stock> findByStockCode(String stockCode);

    boolean existsByStockCode(String stockCode);
}
