package com.tumo.user.dto;

import com.tumo.user.domain.User;

public record MyUserResponse(
        Long id,
        String email,
        String nickname,
        Long cashBalance
) {

    public static MyUserResponse from(User user) {
        return new MyUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCashBalance()
        );
    }
}
