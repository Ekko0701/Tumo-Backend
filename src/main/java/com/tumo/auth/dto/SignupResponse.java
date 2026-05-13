package com.tumo.auth.dto;

import com.tumo.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 회원가입 응답 DTO.
 */
@Schema(description = "회원가입 응답")
public record SignupResponse(
        /**
         * 생성된 사용자 ID.
         */
        @Schema(description = "생성된 사용자 ID", example = "1")
        Long id,

        /**
         * 가입한 사용자 이메일.
         */
        @Schema(description = "가입한 사용자 이메일", example = "user@example.com")
        String email,

        /**
         * 앱에서 표시할 사용자 닉네임.
         */
        @Schema(description = "앱에서 표시할 사용자 닉네임", example = "투자왕")
        String nickname,

        /**
         * 가입 직후 지급된 가상 현금 잔고.
         */
        @Schema(description = "가입 직후 지급된 가상 현금 잔고", example = "10000000")
        Long cashBalance
) {

    /**
     * 저장된 User 엔티티를 회원가입 응답으로 변환한다.
     */
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getCashBalance()
        );
    }
}
