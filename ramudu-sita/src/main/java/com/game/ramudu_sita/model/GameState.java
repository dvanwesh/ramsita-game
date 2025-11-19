package com.game.ramudu_sita.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class GameState {
    private final String id;
    private final String code;
    private GameStatus status;
    private int totalRounds;
    private int currentRoundNumber;

    private final Map<String, Player> players = new LinkedHashMap<>(); // preserve join order
    private final Map<Integer, RoundState> rounds = new HashMap<>();

    public GameState(String id, String code, int totalRounds) {
        this.id = id;
        this.code = code;
        this.totalRounds = totalRounds;
        this.status = GameStatus.LOBBY;
        this.currentRoundNumber = 0;
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
}
