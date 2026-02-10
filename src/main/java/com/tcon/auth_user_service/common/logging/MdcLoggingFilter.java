package com.tcon.auth_user_service.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String MDC_CORRELATION_ID_KEY = "correlationId";
    private static final String MDC_USER_ID_KEY = "userId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long start = System.currentTimeMillis();
        String correlationId = null;
        String userId = null;

        try {
            // 1) correlationId
            correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }
            MDC.put(MDC_CORRELATION_ID_KEY, correlationId);
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            // 2) userId from SecurityContext
            userId = resolveCurrentUserId();
            if (userId != null) {
                MDC.put(MDC_USER_ID_KEY, userId);
            }

            log.info("REQUEST {} {} userId={} traceId={}",
                    request.getMethod(), request.getRequestURI(), userId, correlationId);

            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("RESPONSE {} {} status={} durationMs={} userId={} traceId={}",
                    request.getMethod(), request.getRequestURI(),
                    response.getStatus(), duration, userId, correlationId);

            MDC.clear();
        }
    }

    private String resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof String s) {
            return s; // matches @AuthenticationPrincipal String userId
        }
        return auth.getName();
    }
}
