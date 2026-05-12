package com.tumo.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /**
     * 요청 본문, 쿼리 파라미터, 경로 변수 등의 입력값 검증에 실패한 경우.
     */
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청값이 올바르지 않습니다."),

    /**
     * 회원가입 시 이미 사용 중인 이메일로 가입을 시도한 경우.
     */
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "DUPLICATED_EMAIL", "이미 사용 중인 이메일입니다."),

    /**
     * 로그인 시 이메일 또는 비밀번호가 일치하지 않는 경우.
     */
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "INVALID_LOGIN", "이메일 또는 비밀번호가 올바르지 않습니다."),

    /**
     * 서버 내부에서 예상하지 못한 오류가 발생한 경우.
     */
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "서버 내부 오류가 발생했습니다."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;
}
