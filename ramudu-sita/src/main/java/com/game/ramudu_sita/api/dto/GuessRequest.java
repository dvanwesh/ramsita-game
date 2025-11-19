package com.game.ramudu_sita.api.dto;

public class GuessRequest {
    private String playerId;        // Ramudu
    private String guessedPlayerId; // suspected Sita

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getGuessedPlayerId() {
        return guessedPlayerId;
    }

    public void setGuessedPlayerId(String guessedPlayerId) {
        this.guessedPlayerId = guessedPlayerId;
    }
}
