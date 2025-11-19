package com.game.ramudu_sita.api.dto;

public class GuessRequest {
    private String guessedPlayerId; // suspected Sita

    public String getGuessedPlayerId() {
        return guessedPlayerId;
    }

    public void setGuessedPlayerId(String guessedPlayerId) {
        this.guessedPlayerId = guessedPlayerId;
    }
}
