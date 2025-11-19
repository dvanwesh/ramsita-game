package com.game.ramudu_sita.rate;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very simple in-memory fixed-window rate limiter.
 * Suitable for single-node / sticky-session setups.
 */
@Service
public class RateLimiterService {

    private static class Counter {
        long windowStartEpochSeconds;
        int count;
    }

    // key -> counter
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    /**
     * Check rate and throw if exceeded.
     *
     * @param key           unique key, e.g. "CREATE:ip" or "JOIN:ip"
     * @param limit         max allowed in the window
     * @param windowSeconds window size in seconds
     */
    public void checkRate(String key, int limit, long windowSeconds) {
        long now = Instant.now().getEpochSecond();
        Counter counter = counters.computeIfAbsent(key, k -> {
            Counter c = new Counter();
            c.windowStartEpochSeconds = now;
            c.count = 0;
            return c;
        });

        synchronized (counter) {
            // if outside of window, reset
            if (now - counter.windowStartEpochSeconds >= windowSeconds) {
                counter.windowStartEpochSeconds = now;
                counter.count = 0;
            }

            if (counter.count >= limit) {
                throw new RateLimitExceededException("Rate limit exceeded for key: " + key);
            }

            counter.count++;
        }
    }

    @Profile("test")
    public void clearAllForTests() {
        counters.clear();
    }
}
