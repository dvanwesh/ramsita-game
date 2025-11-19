package com.game.ramudu_sita.model;

import java.util.HashMap;
import java.util.Map;

public class RoundState {
    private final int roundNumber;
    private RoundStatus status;

    // playerId -> chit
    private final Map<String, ChitType> assignments = new HashMap<>();

    private String ramuduPlayerId;
    private String sitaPlayerId;
    private String guessTargetPlayerId; // whom Ramudu guessed

    // per-round score delta
    private Map<String, Integer> scoreDelta = new HashMap<>();

    public RoundState(int roundNumber) {
        this.roundNumber = roundNumber;
        this.status = RoundStatus.DISTRIBUTING;
    }

    public int getRoundNumber() { return roundNumber; }
    public RoundStatus getStatus() { return status; }
    public void setStatus(RoundStatus status) { this.status = status; }

    public Map<String, ChitType> getAssignments() { return assignments; }

    public String getRamuduPlayerId() { return ramuduPlayerId; }
    public void setRamuduPlayerId(String ramuduPlayerId) { this.ramuduPlayerId = ramuduPlayerId; }

    public String getSitaPlayerId() { return sitaPlayerId; }
    public void setSitaPlayerId(String sitaPlayerId) { this.sitaPlayerId = sitaPlayerId; }

    public String getGuessTargetPlayerId() { return guessTargetPlayerId; }
    public void setGuessTargetPlayerId(String guessTargetPlayerId) { this.guessTargetPlayerId = guessTargetPlayerId; }

    public Map<String, Integer> getScoreDelta() { return scoreDelta; }
    public void setScoreDelta(Map<String, Integer> scoreDelta) { this.scoreDelta = scoreDelta; }
}
