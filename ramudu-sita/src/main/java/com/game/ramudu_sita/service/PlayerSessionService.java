package com.game.ramudu_sita.service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSessionService {

    public record PlayerSession(
            String token,
            String gameId,
            String playerId,
            Instant expiresAt
    ) {
    }

    private final Map<String, PlayerSession> sessions = new ConcurrentHashMap<>();

    // 30 minutes default expiry
    private static final long SESSION_TTL_SECONDS = 60 * 30;

    public PlayerSession createSession(String gameId, String playerId) {
        String token = UUID.randomUUID().toString() + "-" + UUID.randomUUID();
        PlayerSession session = new PlayerSession(
                token,
                gameId,
                playerId,
                Instant.now().plusSeconds(SESSION_TTL_SECONDS)
        );
        sessions.put(token, session);
        return session;
    }

    public PlayerSession requireValidSession(String token, String gameId) {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Missing player session token");
        }
        PlayerSession s = sessions.get(token);
        if (s == null || s.expiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Player session expired or invalid");
        }
        if (!s.gameId().equals(gameId)) {
            throw new IllegalStateException("Player does not belong to this game");
        }
        return s;
    }

    public void cleanupExpiredSessions() {
        Instant now = Instant.now();
        sessions.values().removeIf(s -> s.expiresAt().isBefore(now));
    }
}

