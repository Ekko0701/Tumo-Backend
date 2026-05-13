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
     * 요청한 HTTP Method를 지원하지 않는 경우.
     */
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다."),

    /**
     * 요청한 API 경로 또는 리소스를 찾을 수 없는 경우.
     */
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),

    /**
     * 회원가입 시 이미 사용 중인 이메일로 가입을 시도한 경우.
     */
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "DUPLICATED_EMAIL", "이미 사용 중인 이메일입니다."),

    /**
     * 로그인 시 이메일 또는 비밀번호가 일치하지 않는 경우.
     */
    INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "INVALID_LOGIN", "이메일 또는 비밀번호가 올바르지 않습니다."),

    /**
     * 인증 토큰이 없거나, 만료되었거나, 서명이 올바르지 않은 경우.
     */
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "인증 토큰이 유효하지 않습니다."),

    /**
     * 인증된 사용자 식별자에 해당하는 회원을 찾을 수 없는 경우.
     */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),

    /**
     * 요청한 종목 코드에 해당하는 종목을 찾을 수 없는 경우.
     */
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK_NOT_FOUND", "종목을 찾을 수 없습니다."),

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
