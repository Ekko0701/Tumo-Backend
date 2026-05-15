package com.tumo.portfolio.service;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import com.tumo.holding.repository.HoldingRepository;
import com.tumo.portfolio.dto.PortfolioHoldingResponse;
import com.tumo.portfolio.dto.PortfolioResponse;
import com.tumo.user.domain.User;
import com.tumo.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private static final long INITIAL_TOTAL_ASSET = 10_000_000L;

    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;

    public PortfolioResponse getMyPortfolio(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<PortfolioHoldingResponse> holdings = holdingRepository.findAllByUser(user).stream()
                .map(PortfolioHoldingResponse::from)
                .toList();

        Long totalStockValue = holdings.stream()
                .mapToLong(PortfolioHoldingResponse::evaluationAmount)
                .sum();
        Long totalAsset = user.getCashBalance() + totalStockValue;
        Long profitAmount = totalAsset - INITIAL_TOTAL_ASSET;
        Double profitRate = profitAmount * 100.0 / INITIAL_TOTAL_ASSET;

        return new PortfolioResponse(
                user.getCashBalance(),
                totalStockValue,
                totalAsset,
                profitAmount,
                profitRate,
                holdings
        );
    }
}
