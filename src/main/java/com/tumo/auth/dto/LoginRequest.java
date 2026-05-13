package com.tumo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO.
 */
@Schema(description = "로그인 요청")
public record LoginRequest(
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
        String password
) {
}
