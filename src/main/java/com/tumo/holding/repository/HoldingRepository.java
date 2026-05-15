package com.tumo.holding.repository;

import com.tumo.holding.domain.Holding;
import com.tumo.stock.domain.Stock;
import com.tumo.user.domain.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

    Optional<Holding> findByUserAndStock(User user, Stock stock);

    List<Holding> findAllByUser(User user);
}
