package com.tumo.global.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleHttpMessageNotReadableException() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpMessageNotReadableException(
                new HttpMessageNotReadableException("Required request body is missing", null)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.INVALID_REQUEST.getCode());
        assertThat(response.getBody().fieldErrors()).isEmpty();
    }

    @Test
    void handleMissingServletRequestParameterException() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMissingServletRequestParameterException(
                new MissingServletRequestParameterException("email", "String")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.INVALID_REQUEST.getCode());
        assertThat(response.getBody().fieldErrors()).hasSize(1);
        assertThat(response.getBody().fieldErrors().getFirst().field()).isEqualTo("email");
    }

    @Test
    void handleHttpRequestMethodNotSupportedException() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpRequestMethodNotSupportedException(
                new HttpRequestMethodNotSupportedException("GET")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.METHOD_NOT_ALLOWED.getCode());
    }

    @Test
    void handleNotFoundException() {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNotFoundException(
                new NoResourceFoundException(HttpMethod.GET, "/unknown", null)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.NOT_FOUND.getCode());
    }
}
