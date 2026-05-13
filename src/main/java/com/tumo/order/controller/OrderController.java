package com.tumo.order.controller;

import com.tumo.order.dto.OrderRequest;
import com.tumo.order.dto.OrderResponse;
import com.tumo.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
@Tag(name = "Order", description = "주문 API")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "매수 주문", description = "현재가 기준으로 시장가 매수 주문을 즉시 체결합니다.")
    public OrderResponse buy(
            Authentication authentication,
            @Valid @RequestBody OrderRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();

        return orderService.buy(userId, request);
    }
}
