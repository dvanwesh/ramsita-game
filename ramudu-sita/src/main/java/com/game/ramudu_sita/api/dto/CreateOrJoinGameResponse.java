package com.game.ramudu_sita.api.dto;

public class CreateOrJoinGameResponse {
    private String gameId;
    private String gameCode;
    private String playerId;

    private String playerToken;

    public CreateOrJoinGameResponse(String gameId, String gameCode, String playerId, String playerToken) {
        this.gameId = gameId;
        this.gameCode = gameCode;
        this.playerId = playerId;
        this.playerToken = playerToken;
    }

    public String getGameId() {
        return gameId;
    }

    public String getGameCode() {
        return gameCode;
    }

    public String getPlayerId() {
        return playerId;
    }
}
