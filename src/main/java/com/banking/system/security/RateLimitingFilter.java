package com.banking.system.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000L;
    private final Map<String, RequestWindow> windowMap = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().contains("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = getClientIp(request);
        RequestWindow window = windowMap.computeIfAbsent(ip, k -> new RequestWindow());
        if (!window.isAllowed()) {
            log.warn("Rate limit exceeded for IP: {}", ip);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {"status":429,"error":"Too Many Requests",
                     "message":"Rate limit exceeded. Try again in 60 seconds."}
                    """);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isEmpty())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }

    static class RequestWindow {
        private final long[] timestamps = new long[MAX_REQUESTS];
        private int index = 0;
        private int count = 0;

        synchronized boolean isAllowed() {
            long now = Instant.now().toEpochMilli();
            if (count < MAX_REQUESTS) {
                timestamps[index] = now;
                index = (index + 1) % MAX_REQUESTS;
                count++;
                return true;
            }
            int oldest = index;
            if (now - timestamps[oldest] > WINDOW_MS) {
                timestamps[index] = now;
                index = (index + 1) % MAX_REQUESTS;
                return true;
            }
            return false;
        }
    }
}