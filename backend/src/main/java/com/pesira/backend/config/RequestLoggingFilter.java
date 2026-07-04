package com.pesira.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String START_TIME_ATTRIBUTE = "requestStartTime";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, startTime);

        log.info("Incoming request: {} {}", request.getMethod(), sanitizePath(request.getRequestURI()));

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Completed request: {} {} status={} durationMs={}",
                    request.getMethod(),
                    sanitizePath(request.getRequestURI()),
                    response.getStatus(),
                    duration);
        }
    }

    private String sanitizePath(String path) {
        return path.replaceAll("(?i)(token|password|secret)=[^&]*", "$1=***");
    }
}
