package com.tumo.portfolio.controller;

import com.tumo.portfolio.dto.PortfolioResponse;
import com.tumo.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/portfolio")
@Tag(name = "Portfolio", description = "포트폴리오 API")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    @Operation(summary = "내 포트폴리오 조회", description = "로그인한 사용자의 현금 잔고, 보유 종목, 평가손익을 조회합니다.")
    public PortfolioResponse getMyPortfolio(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        return portfolioService.getMyPortfolio(userId);
    }
}
