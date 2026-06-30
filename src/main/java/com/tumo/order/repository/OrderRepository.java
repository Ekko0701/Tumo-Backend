package com.tumo.order.repository;

import com.tumo.order.domain.Order;
import com.tumo.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserOrderByExecutedAtDesc(User user, Pageable pageable);
}
