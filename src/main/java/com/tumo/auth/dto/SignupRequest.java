package com.tumo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 DTO.
 */
@Schema(description = "회원가입 요청")
public record SignupRequest(
        /**
         * 로그인에 사용할 이메일.
         */
        @Schema(description = "로그인에 사용할 이메일", example = "user@example.com")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        /**
         * 로그인에 사용할 비밀번호.
         */
        @Schema(description = "로그인에 사용할 비밀번호", example = "password1234")
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
        String password,

        /**
         * 앱에서 표시할 사용자 닉네임.
         */
        @Schema(description = "앱에서 표시할 사용자 닉네임", example = "투자왕")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
        String nickname
) {
}
