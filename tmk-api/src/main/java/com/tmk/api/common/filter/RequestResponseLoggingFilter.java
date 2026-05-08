package com.tmk.api.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_BODY_LENGTH = 800;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.endsWith("/events")
                || uri.endsWith(".css")
                || uri.endsWith(".js")
                || uri.endsWith(".html")
                || uri.equals("/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            String method = wrappedRequest.getMethod();
            String uri = wrappedRequest.getRequestURI();
            String query = wrappedRequest.getQueryString();
            String endpoint = query == null ? uri : uri + "?" + query;
            int status = wrappedResponse.getStatus();

            log.info("[HTTP] {} {} -> {} ({} ms)", method, endpoint, status, elapsed);

            String requestBody = extractRequestBody(wrappedRequest);
            if (!requestBody.isBlank()) {
                log.info("[HTTP][REQUEST] {} {} body={}", method, endpoint, requestBody);
            }

            String responseBody = extractResponseBody(wrappedResponse);
            if (!responseBody.isBlank()) {
                log.info("[HTTP][RESPONSE] {} {} status={} body={}", method, endpoint, status, responseBody);
            }

            wrappedResponse.copyBodyToResponse();
        }
    }

    private String extractRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        return isLoggableBody(request.getContentType(), content)
                ? abbreviate(new String(content, StandardCharsets.UTF_8))
                : "";
    }

    private String extractResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        return isLoggableBody(response.getContentType(), content)
                ? abbreviate(new String(content, StandardCharsets.UTF_8))
                : "";
    }

    private boolean isLoggableBody(String contentType, byte[] content) {
        if (content == null || content.length == 0 || contentType == null) {
            return false;
        }
        return contentType.contains(MediaType.APPLICATION_JSON_VALUE)
                || contentType.contains(MediaType.TEXT_PLAIN_VALUE);
    }

    private String abbreviate(String body) {
        String normalized = body.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= MAX_BODY_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_BODY_LENGTH) + "...";
    }
}
