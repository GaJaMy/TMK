package com.tmk.api.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmk.api.common.SecurityResponseWriter;
import com.tmk.core.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        SecurityResponseWriter.write(response, objectMapper, ErrorCode.UNAUTHORIZED);
    }
}