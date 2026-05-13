package com.tumo.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.tumo.global.error.ErrorCode;
import com.tumo.global.error.ErrorResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import tools.jackson.databind.ObjectMapper;

class JwtAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint = new JwtAuthenticationEntryPoint(objectMapper);

    @Test
    void commence() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationEntryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException("authentication required")
        );

        ErrorResponse errorResponse = objectMapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(response.getStatus()).isEqualTo(ErrorCode.INVALID_TOKEN.getStatus().value());
        assertThat(MediaType.parseMediaType(response.getContentType()).isCompatibleWith(MediaType.APPLICATION_JSON))
                .isTrue();
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
        assertThat(errorResponse.code()).isEqualTo(ErrorCode.INVALID_TOKEN.getCode());
        assertThat(errorResponse.message()).isEqualTo(ErrorCode.INVALID_TOKEN.getMessage());
        assertThat(errorResponse.fieldErrors()).isEmpty();
    }
}
