package com.tumo.global.error;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<FieldError> fieldErrors
) {

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                List.of()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> fieldErrors) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                fieldErrors
        );
    }

    public record FieldError(
            String field,
            String message
    ) {
    }
}
