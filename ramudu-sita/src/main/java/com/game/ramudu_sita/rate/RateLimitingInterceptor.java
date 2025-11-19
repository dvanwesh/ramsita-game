package com.game.ramudu_sita.rate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class RateLimitingInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    public RateLimitingInterceptor(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Extract client key (rough, but works behind ALB if X-Forwarded-For is set)
        String ip = extractClientIp(request);

        // Basic rules (tweak as needed):

        // 1) Create game: at most 10 per minute per IP
        if (HttpMethod.POST.matches(method) && path.equals("/api/games")) {
            String key = "CREATE_GAME:" + ip;
            rateLimiterService.checkRate(key, 10, 60);
        }

        // 2) Join game: at most 60 per minute per IP
        else if (HttpMethod.POST.matches(method) && path.equals("/api/games/join")) {
            String key = "JOIN_GAME:" + ip;
            rateLimiterService.checkRate(key, 60, 60);
        }

        // 3) Guess: at most 20 per minute per IP (Ramudu spamming guesses)
        else if (HttpMethod.POST.matches(method) && path.matches("^/api/games/.+/rounds/current/guess$")) {
            String key = "GUESS:" + ip;
            rateLimiterService.checkRate(key, 20, 60);
        }

        // 4) Start game: at most 20 per minute per IP
        else if (HttpMethod.POST.matches(method) && path.matches("^/api/games/.+/start$")) {
            String key = "START:" + ip;
            rateLimiterService.checkRate(key, 20, 60);
        }

        return true;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
