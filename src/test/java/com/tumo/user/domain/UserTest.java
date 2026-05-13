package com.tumo.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tumo.global.error.BusinessException;
import com.tumo.global.error.ErrorCode;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void decreaseCashBalance() {
        User user = new User("test@example.com", "encoded-password", "tester");

        user.decreaseCashBalance(750000L);

        assertThat(user.getCashBalance()).isEqualTo(9250000L);
    }

    @Test
    void decreaseCashBalanceThrowsExceptionWhenCashBalanceIsInsufficient() {
        User user = new User("test@example.com", "encoded-password", "tester");

        assertThatThrownBy(() -> user.decreaseCashBalance(10000001L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_CASH)
                );
    }
}
