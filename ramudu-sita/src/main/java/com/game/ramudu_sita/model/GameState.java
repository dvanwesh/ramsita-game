package com.game.ramudu_sita.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class GameState {
    private final String id;
    private final String code;
    private GameStatus status;
    private int totalRounds;
    private int currentRoundNumber;
    private final Instant createdAt;
    private Instant lastActivityAt;

    private String creatorKey;    // e.g. IP or session key

    private final Map<String, Player> players = new LinkedHashMap<>(); // preserve join order
    private final Map<Integer, RoundState> rounds = new HashMap<>();

    public GameState(String id, String code, int totalRounds) {
        this.id = id;
        this.code = code;
        this.totalRounds = totalRounds;
        this.status = GameStatus.LOBBY;
        this.currentRoundNumber = 0;
        this.createdAt = Instant.now();
        this.lastActivityAt = this.createdAt;
    }

    public String getId() { return id; }
    public String getCode() { return code; }

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }

    public int getTotalRounds() { return totalRounds; }
    public int getCurrentRoundNumber() { return currentRoundNumber; }
    public void setCurrentRoundNumber(int currentRoundNumber) { this.currentRoundNumber = currentRoundNumber; }

    public Map<String, Player> getPlayers() { return players; }
    public Map<Integer, RoundState> getRounds() { return rounds; }

    public RoundState getCurrentRound() {
        return rounds.get(currentRoundNumber);
    }

    public void touch() {
        this.lastActivityAt = Instant.now();
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public String getCreatorKey() {
        return creatorKey;
    }

    public void setCreatorKey(String creatorKey) {
        this.creatorKey = creatorKey;
    }
}
