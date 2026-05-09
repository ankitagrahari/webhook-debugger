package org.backendbrilliance.uiservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration(proxyBeanMethods = false)
public class RateLimiterConfig implements WebMvcConfigurer {

    // Max 5 registration attempts per IP per hour
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60 * 60 * 1000L; // 1 hour

    private final Map<String, long[]> attempts = new ConcurrentHashMap<>();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object handler) throws Exception {
                if (!request.getMethod().equals("POST") ||
                        !request.getRequestURI().equals("/api/auth/register")) {
                    return true;
                }

                String ip = getClientIp(request);
                long now = System.currentTimeMillis();

                attempts.compute(ip, (k, v) -> {
                    if (v == null || now - v[1] > WINDOW_MS) {
                        return new long[]{1, now}; // [count, windowStart]
                    }
                    v[0]++;
                    return v;
                });

                long[] data = attempts.get(ip);
                if (data[0] > MAX_ATTEMPTS) {
                    response.setStatus(429);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"error\":\"RATE_LIMIT\",\"message\":\"Too many registration attempts. Try again in an hour.\"}"
                    );
                    return false;
                }

                return true;
            }
        });
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim(); // first IP in chain
        }
        return request.getRemoteAddr();
    }
}
