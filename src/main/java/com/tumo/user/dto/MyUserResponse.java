package com.tumo.user.dto;

import com.tumo.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 정보 응답")
public record MyUserResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long id,

        @Schema(description = "사용자 이메일", example = "user@example.com")
        String email,

        @Schema(description = "앱에서 표시할 사용자 닉네임", example = "투자왕")
        String nickname,

        @Schema(description = "현재 가상 현금 잔고", example = "10000000")
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
