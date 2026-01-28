package com.tcon.auth_user_service.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);

            if (StringUtils.hasText(token)) {
                log.debug("🔍 JWT token found, validating...");

                if (!jwtTokenProvider.isTokenExpired(token)) {
                    String userId = jwtTokenProvider.getUserId(token);
                    String role = jwtTokenProvider.getRole(token).name();
                    String email = jwtTokenProvider.getEmail(token);

                    log.info("✅ JWT valid for user: {} ({}), role: {}", email, userId, role);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("✅ Authentication set in SecurityContext");
                } else {
                    log.warn("❌ JWT token is expired");
                }
            } else {
                log.debug("No JWT token found in request");
            }
        } catch (Exception ex) {
            log.error("❌ JWT authentication failed: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract token and remove ALL whitespace (including line breaks, spaces, tabs)
            String token = bearerToken.substring(7).trim().replaceAll("\\s+", "");
            log.debug("Extracted JWT token (length: {})", token.length());
            return token;
        }

        return null;
    }
}
