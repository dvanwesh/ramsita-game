package com.game.ramudu_sita.api.dto;

import com.game.ramudu_sita.model.ChitType;
import com.game.ramudu_sita.model.GameStatus;
import com.game.ramudu_sita.model.RoundStatus;

import java.util.List;
import java.util.Map;

public class MyStateResponse {
    public record PlayerView(String id, String name, boolean host, int totalScore) {
    }

    private String gameId;
    private String gameCode;
    private GameStatus gameStatus;
    private int totalRounds;
    private int currentRoundNumber;

    private PlayerView me;
    private List<PlayerView> players;

    private ChitType myChit;                // null if no active round yet
    private RoundStatus roundStatus;
    private Map<String, Integer> lastRoundScoreDelta; // optional

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

    public PlayerView getMe() {
        return me;
    }

    public void setMe(PlayerView me) {
        this.me = me;
    }

    public List<PlayerView> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerView> players) {
        this.players = players;
    }

    public ChitType getMyChit() {
        return myChit;
    }

    public void setMyChit(ChitType myChit) {
        this.myChit = myChit;
    }

    public RoundStatus getRoundStatus() {
        return roundStatus;
    }

    public void setRoundStatus(RoundStatus roundStatus) {
        this.roundStatus = roundStatus;
    }

    public Map<String, Integer> getLastRoundScoreDelta() {
        return lastRoundScoreDelta;
    }

    public void setLastRoundScoreDelta(Map<String, Integer> lastRoundScoreDelta) {
        this.lastRoundScoreDelta = lastRoundScoreDelta;
    }
}
