package com.tumo.auth.dto;

import com.tumo.user.domain.User;

/**
 * 회원가입 응답 DTO.
 */
public record SignupResponse(
        /**
         * 생성된 사용자 ID.
         */
        Long id,

        /**
         * 가입한 사용자 이메일.
         */
        String email,

        /**
         * 앱에서 표시할 사용자 닉네임.
         */
        String nickname,

        /**
         * 가입 직후 지급된 가상 현금 잔고.
         */
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
