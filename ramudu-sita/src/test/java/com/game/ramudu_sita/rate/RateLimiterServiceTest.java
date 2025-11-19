package com.game.ramudu_sita.rate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterServiceTest {

    @Test
    void shouldAllowRequestsWithinLimit() {
        RateLimiterService limiter = new RateLimiterService();

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                limiter.checkRate("TEST", 5, 60);
            }
        });
    }

    @Test
    void shouldRejectWhenLimitExceeded() {
        RateLimiterService limiter = new RateLimiterService();

        for (int i = 0; i < 5; i++) {
            limiter.checkRate("TEST2", 5, 60);
        }

        assertThrows(
                RateLimitExceededException.class,
                () -> limiter.checkRate("TEST2", 5, 60)
        );
    }

    @Test
    void shouldResetAfterWindow() throws InterruptedException {
        RateLimiterService limiter = new RateLimiterService();

        limiter.checkRate("RESET", 1, 1);

        // next call exceeds limit → exception
        assertThrows(
                RateLimitExceededException.class,
                () -> limiter.checkRate("RESET", 1, 1)
        );

        // wait for window to expire
        Thread.sleep(1100);

        // should pass now
        assertDoesNotThrow(() -> limiter.checkRate("RESET", 1, 1));
    }
}
