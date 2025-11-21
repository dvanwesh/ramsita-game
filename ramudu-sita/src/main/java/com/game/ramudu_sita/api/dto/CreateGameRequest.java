package com.game.ramudu_sita.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class CreateGameRequest {
    private String playerName;

    @Min(1)
    @Max(10)
    private int totalRounds;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }
}
