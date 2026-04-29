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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ADMIN_API_PREFIX = "/admin/";

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

            String role = claims.get("role", String.class);
            Long principalId = claims.get("principalId", Long.class);
            String principalType = claims.get("principalType", String.class);
            String username = claims.getSubject();

            if (isAdminRequest(request) && !AuthenticatedPrincipal.ADMIN_PRINCIPAL_TYPE.equals(principalType)) {
                SecurityResponseWriter.write(response, objectMapper, ErrorCode.FORBIDDEN);
                return;
            }

            AuthenticatedPrincipal userDetails = new AuthenticatedPrincipal(
                    username,
                    null,
                    principalId,
                    role,
                    principalType
            );
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            SecurityResponseWriter.write(response, objectMapper, ErrorCode.EXPIRED_ACCESS_TOKEN);
        } catch (JwtException e) {
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

    private boolean isAdminRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith(ADMIN_API_PREFIX);
    }
}
