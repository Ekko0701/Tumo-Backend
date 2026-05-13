package com.tumo.stock.repository;

import com.tumo.stock.domain.Stock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findByStockCode(String stockCode);

    boolean existsByStockCode(String stockCode);
}
