package com.game.ramudu_sita.api.dto;


import com.game.ramudu_sita.model.GameStatus;
import com.game.ramudu_sita.model.RoundStatus;

import java.util.List;
import java.util.Map;

public class GamePublicState {

    public record PlayerSummary(String id, String name, boolean host, int totalScore) {
    }

    private String gameId;
    private String gameCode;
    private GameStatus gameStatus;
    private int totalRounds;
    private int currentRoundNumber;

    private List<PlayerSummary> players;

    private RoundStatus currentRoundStatus;
    private Map<String, Integer> lastRoundScoreDelta; // playerId -> score delta

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameCode() {
        return gameCode;
    }

    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }

    public int getCurrentRoundNumber() {
        return currentRoundNumber;
    }

    public void setCurrentRoundNumber(int currentRoundNumber) {
        this.currentRoundNumber = currentRoundNumber;
    }

    public List<PlayerSummary> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerSummary> players) {
        this.players = players;
    }

    public RoundStatus getCurrentRoundStatus() {
        return currentRoundStatus;
    }

    public void setCurrentRoundStatus(RoundStatus currentRoundStatus) {
        this.currentRoundStatus = currentRoundStatus;
    }

    public Map<String, Integer> getLastRoundScoreDelta() {
        return lastRoundScoreDelta;
    }

    public void setLastRoundScoreDelta(Map<String, Integer> lastRoundScoreDelta) {
        this.lastRoundScoreDelta = lastRoundScoreDelta;
    }
}

