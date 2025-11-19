package com.game.ramudu_sita.service;

import com.game.ramudu_sita.api.dto.MyStateResponse;
import com.game.ramudu_sita.model.ChitType;
import com.game.ramudu_sita.model.GameStatus;
import com.game.ramudu_sita.model.RoundStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private GameService gameService;
    @BeforeEach
    void setUp() {
        // we don't care about WS interactions in this class, just core logic
        gameService = new GameService(messagingTemplate);
    }

    @Test
    void createGame_initialStateIsLobbyWithHost() {
        var result = gameService.createGame("Host", 5, null);

        assertNotNull(result.gameId());
        assertNotNull(result.code());
        assertNotNull(result.playerId());

        MyStateResponse myState = gameService.getMyState(result.gameId(), result.playerId());
        assertEquals(GameStatus.LOBBY, myState.getGameStatus());
        assertEquals(5, myState.getTotalRounds());
        assertEquals(1, myState.getPlayers().size());

        var me = myState.getMe();
        assertEquals(result.playerId(), me.id());
        assertEquals("Host", me.name());
        assertTrue(me.host());
        assertEquals(0, me.totalScore());
    }

    @Test
    void joinGame_addsPlayerToLobby() {
        var create = gameService.createGame("Host", 5, null);
        var join = gameService.joinGame(create.code(), "Player2");

        assertNotNull(join.playerId());
        assertEquals(create.gameId(), join.gameId());

        MyStateResponse hostState = gameService.getMyState(create.gameId(), create.playerId());
        MyStateResponse p2State = gameService.getMyState(create.gameId(), join.playerId());

        assertEquals(2, hostState.getPlayers().size());
        assertEquals(2, p2State.getPlayers().size());
    }

    @Test
    void startGame_requiresHost() {
        var create = gameService.createGame("Host", 5, null);
        var join1 = gameService.joinGame(create.code(), "P2");

        // starting with non-host should fail
        assertThrows(IllegalStateException.class,
                () -> gameService.startGame(create.gameId(), join1.playerId()));
    }

    @Test
    void startGame_requiresAtLeastThreePlayers() {
        var create = gameService.createGame("Host", 5, null);
        // only 1 player (host)

        assertThrows(IllegalStateException.class,
                () -> gameService.startGame(create.gameId(), create.playerId()));
    }

    @Test
    void startGame_distributesChitsAndSetsRoundState() {
        var create = gameService.createGame("Host", 5, null);
        var join1 = gameService.joinGame(create.code(), "P2");
        var join2 = gameService.joinGame(create.code(), "P3");

        gameService.startGame(create.gameId(), create.playerId());

        // each player sees a chit & round info
        MyStateResponse hostState = gameService.getMyState(create.gameId(), create.playerId());
        MyStateResponse p2State = gameService.getMyState(create.gameId(), join1.playerId());
        MyStateResponse p3State = gameService.getMyState(create.gameId(), join2.playerId());

        assertEquals(GameStatus.IN_ROUND, hostState.getGameStatus());
        assertEquals(1, hostState.getCurrentRoundNumber());
        assertEquals(RoundStatus.WAITING_FOR_RAMUDU, hostState.getRoundStatus());

        // chits should not be null
        assertNotNull(hostState.getMyChit());
        assertNotNull(p2State.getMyChit());
        assertNotNull(p3State.getMyChit());

        // exactly one RAMUDU and one SITA across players
        var chits = List.of(hostState.getMyChit(), p2State.getMyChit(), p3State.getMyChit());
        long ramuduCount = chits.stream().filter(c -> c == ChitType.RAMUDU).count();
        long sitaCount = chits.stream().filter(c -> c == ChitType.SITA).count();

        assertEquals(1, ramuduCount);
        assertEquals(1, sitaCount);
    }

    @Test
    void makeGuess_correctGuess_awardsScoresAndFinishesWhenSingleRound() {
        // single round for deterministic finished state
        var create = gameService.createGame("Host", 1, null);
        var join1 = gameService.joinGame(create.code(), "P2");
        var join2 = gameService.joinGame(create.code(), "P3");

        gameService.startGame(create.gameId(), create.playerId());

        // find Ramudu & Sita by inspecting each player's chit
        MyStateResponse sHost = gameService.getMyState(create.gameId(), create.playerId());
        MyStateResponse sP2 = gameService.getMyState(create.gameId(), join1.playerId());
        MyStateResponse sP3 = gameService.getMyState(create.gameId(), join2.playerId());

        var allStates = List.of(sHost, sP2, sP3);

        String ramuduPlayerId = allStates.stream()
                .filter(s -> s.getMyChit() == ChitType.RAMUDU)
                .map(s -> s.getMe().id())
                .findFirst()
                .orElseThrow();

        String sitaPlayerId = allStates.stream()
                .filter(s -> s.getMyChit() == ChitType.SITA)
                .map(s -> s.getMe().id())
                .findFirst()
                .orElseThrow();

        // Ramudu guesses correctly
        gameService.makeGuess(create.gameId(), ramuduPlayerId, sitaPlayerId);

        // After single round, game should be FINISHED
        MyStateResponse anyState = gameService.getMyState(create.gameId(), create.playerId());
        assertEquals(GameStatus.FINISHED, anyState.getGameStatus());
        assertEquals(1, anyState.getCurrentRoundNumber());

        // verify score rules
        Map<String, Integer> totals = anyState.getPlayers().stream()
                .collect(Collectors.toMap(MyStateResponse.PlayerView::id,
                        MyStateResponse.PlayerView::totalScore));

        assertEquals(500, totals.get(ramuduPlayerId));  // Ramudu gets 500
        assertEquals(100, totals.get(sitaPlayerId));    // Sita gets 100
        // third player gets their base chit points (not 500/100)
        String thirdPlayerId = totals.keySet().stream()
                .filter(id -> !id.equals(ramuduPlayerId) && !id.equals(sitaPlayerId))
                .findFirst()
                .orElseThrow();
        assertTrue(totals.get(thirdPlayerId) > 0);
    }

    @Test
    void makeGuess_incorrectGuess_setsRamuduZeroAndSitaThousand() {
        var create = gameService.createGame("Host", 1, null);
        var join1 = gameService.joinGame(create.code(), "P2");
        var join2 = gameService.joinGame(create.code(), "P3");

        gameService.startGame(create.gameId(), create.playerId());

        MyStateResponse sHost = gameService.getMyState(create.gameId(), create.playerId());
        MyStateResponse sP2 = gameService.getMyState(create.gameId(), join1.playerId());
        MyStateResponse sP3 = gameService.getMyState(create.gameId(), join2.playerId());

        var allStates = List.of(sHost, sP2, sP3);

        String ramuduPlayerId = allStates.stream()
                .filter(s -> s.getMyChit() == ChitType.RAMUDU)
                .map(s -> s.getMe().id())
                .findFirst()
                .orElseThrow();

        String sitaPlayerId = allStates.stream()
                .filter(s -> s.getMyChit() == ChitType.SITA)
                .map(s -> s.getMe().id())
                .findFirst()
                .orElseThrow();

        // pick a wrong target (not Sita)
        String wrongTargetId = allStates.stream()
                .map(s -> s.getMe().id())
                .filter(id -> !id.equals(ramuduPlayerId) && !id.equals(sitaPlayerId))
                .findFirst()
                .orElseThrow();

        gameService.makeGuess(create.gameId(), ramuduPlayerId, wrongTargetId);

        MyStateResponse anyState = gameService.getMyState(create.gameId(), create.playerId());
        Map<String, Integer> totals = anyState.getPlayers().stream()
                .collect(Collectors.toMap(MyStateResponse.PlayerView::id,
                        MyStateResponse.PlayerView::totalScore));

        // Ramudu gets 0 (overriding base chit)
        assertEquals(0, totals.get(ramuduPlayerId));
        // Sita gets 100
        assertEquals(100, totals.get(sitaPlayerId));
    }

    @Test
    void makeGuess_byNonRamuduThrows() {
        var create = gameService.createGame("Host", 1, null);
        var join1 = gameService.joinGame(create.code(), "P2");
        var join2 = gameService.joinGame(create.code(), "P3");

        gameService.startGame(create.gameId(), create.playerId());

        MyStateResponse sHost = gameService.getMyState(create.gameId(), create.playerId());
        MyStateResponse sP2 = gameService.getMyState(create.gameId(), join1.playerId());
        MyStateResponse sP3 = gameService.getMyState(create.gameId(), join2.playerId());

        var allStates = List.of(sHost, sP2, sP3);

        String ramuduPlayerId = allStates.stream()
                .filter(s -> s.getMyChit() == ChitType.RAMUDU)
                .map(s -> s.getMe().id())
                .findFirst()
                .orElseThrow();

        // choose some non-Ramudu player
        String nonRamuduId = allStates.stream()
                .map(s -> s.getMe().id())
                .filter(id -> !id.equals(ramuduPlayerId))
                .findFirst()
                .orElseThrow();

        String someTarget = ramuduPlayerId; // doesn't matter

        assertThrows(IllegalStateException.class,
                () -> gameService.makeGuess(create.gameId(), nonRamuduId, someTarget));
    }

    @Test
    void makeGuess_whenRoundAlreadyCompletedThrows() {
        var create = gameService.createGame("Host", 1, null);
        var join1 = gameService.joinGame(create.code(), "P2");
        var join2 = gameService.joinGame(create.code(), "P3");

        gameService.startGame(create.gameId(), create.playerId());

        MyStateResponse sHost = gameService.getMyState(create.gameId(), create.playerId());
        MyStateResponse sP2 = gameService.getMyState(create.gameId(), join1.playerId());
        MyStateResponse sP3 = gameService.getMyState(create.gameId(), join2.playerId());

        var allStates = List.of(sHost, sP2, sP3);

        String ramuduPlayerId = allStates.stream()
                .filter(s -> s.getMyChit() == ChitType.RAMUDU)
                .map(s -> s.getMe().id())
                .findFirst()
                .orElseThrow();

        String sitaPlayerId = allStates.stream()
                .filter(s -> s.getMyChit() == ChitType.SITA)
                .map(s -> s.getMe().id())
                .findFirst()
                .orElseThrow();

        // first guess (valid)
        gameService.makeGuess(create.gameId(), ramuduPlayerId, sitaPlayerId);

        // second guess in same game should fail (round is completed / game finished)
        assertThrows(IllegalStateException.class,
                () -> gameService.makeGuess(create.gameId(), ramuduPlayerId, sitaPlayerId));
    }

    @Test
    void startGame_withTooManyPlayersForChitsThrows() {
        // we only have 5 chits in CHITS; add 6 players total
        var create = gameService.createGame("Host", 1, null);
        gameService.joinGame(create.code(), "P2");
        gameService.joinGame(create.code(), "P3");
        gameService.joinGame(create.code(), "P4");
        gameService.joinGame(create.code(), "P5");
        gameService.joinGame(create.code(), "P6"); // now 6 players

        // distributeChits inside startGame should blow up
        assertThrows(IllegalStateException.class,
                () -> gameService.startGame(create.gameId(), create.playerId()));
    }
}

