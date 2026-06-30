package com.tumo.order.controller;

import com.tumo.order.dto.OrderPageResponse;
import com.tumo.order.dto.OrderRequest;
import com.tumo.order.dto.OrderResponse;
import com.tumo.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    @Operation(summary = "주문 체결", description = "현재가 기준으로 시장가 매수(BUY)/매도(SELL) 주문을 즉시 체결합니다.")
    public OrderResponse order(
            Authentication authentication,
            @Valid @RequestBody OrderRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();

        return orderService.order(userId, request);
    }

    @GetMapping
    @Operation(summary = "주문 내역 조회", description = "로그인한 사용자의 매수/매도 주문 내역을 최신순으로 page 단위 조회합니다.")
    public OrderPageResponse getOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        Long userId = (Long) authentication.getPrincipal();

        return orderService.getOrders(userId, page, size);
    }
}
