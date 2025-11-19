package com.game.ramudu_sita.api.dto;

public class JoinGameRequest {
    private String code;
    private String playerName;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
