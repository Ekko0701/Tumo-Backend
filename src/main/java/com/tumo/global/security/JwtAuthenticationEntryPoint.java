package com.tumo.global.security;

import com.tumo.global.error.ErrorCode;
import com.tumo.global.error.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * 인증되지 않은 요청이 보호 API에 접근했을 때 Spring Security가 호출하는 메서드.
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        // Security 필터 영역의 인증 실패를 공통 에러 응답 형식으로 변환.
        ErrorResponse errorResponse = ErrorResponse.from(ErrorCode.INVALID_TOKEN);

        // Controller까지 도달하지 못한 인증 실패 응답은 HttpServletResponse에 직접 작성.
        response.setStatus(ErrorCode.INVALID_TOKEN.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
