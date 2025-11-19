package com.game.ramudu_sita.service;

import com.game.ramudu_sita.api.dto.GamePublicState;
import com.game.ramudu_sita.api.dto.MyStateResponse;
import com.game.ramudu_sita.model.GameStatus;
import com.game.ramudu_sita.model.RoundStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceWebSocketEventsTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = new GameService(messagingTemplate);
    }

    @Test
    void createGame_broadcastsLobbyStateToTopic() {
        var result = gameService.createGame("Host", 3, null);

        ArgumentCaptor<String> destCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<GamePublicState> payloadCaptor = ArgumentCaptor.forClass(GamePublicState.class);

        verify(messagingTemplate, atLeastOnce())
                .convertAndSend(destCaptor.capture(), payloadCaptor.capture());

        String destination = destCaptor.getValue();
        GamePublicState state = payloadCaptor.getValue();

        assertEquals("/topic/games/" + result.gameId() + "/state", destination);
        assertEquals(result.gameId(), state.getGameId());
        assertEquals(result.code(), state.getGameCode());
        assertEquals(GameStatus.LOBBY, state.getGameStatus());
        assertEquals(3, state.getTotalRounds());
        assertEquals(1, state.getPlayers().size());
        assertEquals("Host", state.getPlayers().get(0).name());
    }

    @Test
    void joinGame_broadcastsUpdatedLobbyStateWithNewPlayer() {
        var create = gameService.createGame("Host", 3, null);

        // reset interactions to only capture joinGame behavior
        reset(messagingTemplate);

        var join = gameService.joinGame(create.code(), "P2");

        ArgumentCaptor<String> destCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<GamePublicState> payloadCaptor = ArgumentCaptor.forClass(GamePublicState.class);

        verify(messagingTemplate, atLeastOnce())
                .convertAndSend(destCaptor.capture(), payloadCaptor.capture());

        String destination = destCaptor.getValue();
        GamePublicState state = payloadCaptor.getValue();

        assertEquals("/topic/games/" + create.gameId() + "/state", destination);
        assertEquals(2, state.getPlayers().size());
        assertTrue(state.getPlayers().stream()
                .anyMatch(p -> p.name().equals("P2")));
        assertEquals(GameStatus.LOBBY, state.getGameStatus());
    }

    @Test
    void startGame_broadcastsInRoundStateAndRoundStatus() {
        var create = gameService.createGame("Host", 3, null);
        gameService.joinGame(create.code(), "P2");
        gameService.joinGame(create.code(), "P3");

        reset(messagingTemplate);

        gameService.startGame(create.gameId(), create.playerId());

        ArgumentCaptor<String> destCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<GamePublicState> payloadCaptor = ArgumentCaptor.forClass(GamePublicState.class);

        verify(messagingTemplate, atLeastOnce())
                .convertAndSend(destCaptor.capture(), payloadCaptor.capture());

        GamePublicState state = payloadCaptor.getValue();

        assertEquals(create.gameId(), state.getGameId());
        assertEquals(GameStatus.IN_ROUND, state.getGameStatus());
        assertEquals(1, state.getCurrentRoundNumber());
        assertEquals(RoundStatus.WAITING_FOR_RAMUDU, state.getCurrentRoundStatus());
        assertEquals(3, state.getPlayers().size());
    }

    @Test
    void makeGuess_broadcastsRevealAndThenNextRoundOrFinished() {
        // Single round to keep it simple and hit FINISHED
        var create = gameService.createGame("Host", 1, null);
        var join1 = gameService.joinGame(create.code(), "P2");
        var join2 = gameService.joinGame(create.code(), "P3");

        gameService.startGame(create.gameId(), create.playerId());

        // Find ramudu and sita using existing API
        MyStateResponse sHost = gameService.getMyState(create.gameId(), create.playerId());
        MyStateResponse sP2 = gameService.getMyState(create.gameId(), join1.playerId());
        MyStateResponse sP3 = gameService.getMyState(create.gameId(), join2.playerId());

        var allStates = java.util.List.of(sHost, sP2, sP3);

        String ramuduPlayerId = allStates.stream()
                .filter(s -> s.getMyChit() != null && s.getMyChit().name().equals("RAMUDU"))
                .map(s -> s.getMe().id())
                .findFirst()
                .orElseThrow();

        String sitaPlayerId = allStates.stream()
                .filter(s -> s.getMyChit() != null && s.getMyChit().name().equals("SITA"))
                .map(s -> s.getMe().id())
                .findFirst()
                .orElseThrow();

        reset(messagingTemplate);

        gameService.makeGuess(create.gameId(), ramuduPlayerId, sitaPlayerId);

        ArgumentCaptor<String> destCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<GamePublicState> payloadCaptor = ArgumentCaptor.forClass(GamePublicState.class);

        verify(messagingTemplate, atLeastOnce())
                .convertAndSend(destCaptor.capture(), payloadCaptor.capture());

        GamePublicState finalState = payloadCaptor.getValue();
        assertEquals(GameStatus.FINISHED, finalState.getGameStatus());
        assertEquals(1, finalState.getCurrentRoundNumber());
        assertNotNull(finalState.getLastRoundScoreDelta());
        assertFalse(finalState.getLastRoundScoreDelta().isEmpty());
    }
}

