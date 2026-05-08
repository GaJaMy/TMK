package com.tmk.api.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmk.api.common.SecurityResponseWriter;
import com.tmk.api.security.AuthenticatedPrincipal;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.cache.TokenBlacklistPort;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String ACCESS_TOKEN_ATTRIBUTE = "accessToken";

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;
    private final TokenBlacklistPort tokenBlacklistPort;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var claims = jwtProvider.parseClaims(token);

            if (tokenBlacklistPort.isBlacklisted(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            request.setAttribute(ACCESS_TOKEN_ATTRIBUTE, token);

            String role = claims.get("role", String.class);
            Long principalId = claims.get("principalId", Long.class);
            String principalType = claims.get("principalType", String.class);
            String username = claims.getSubject();

            AuthenticatedPrincipal userDetails = new AuthenticatedPrincipal(
                    username,
                    null,
                    principalId,
                    role,
                    principalType
            );
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT access token", e);
            SecurityResponseWriter.write(response, objectMapper, ErrorCode.EXPIRED_ACCESS_TOKEN);
        } catch (JwtException e) {
            log.warn("Invalid JWT access token", e);
            SecurityResponseWriter.write(response, objectMapper, ErrorCode.INVALID_ACCESS_TOKEN);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
