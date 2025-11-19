package com.game.ramudu_sita.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.ramudu_sita.api.dto.GamePublicState;
import com.game.ramudu_sita.model.GameStatus;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end WebSocket + HTTP integration tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class GameWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private WebSocketStompClient stompClient;
    private StompSession session;

    // ---- Helpers ----

    /**
     * Extract a Cookie object for PLAYER_TOKEN from the Set-Cookie header.
     */
    private Cookie extractPlayerCookie(MvcResult result) throws Exception {
        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertNotNull(setCookie, "Set-Cookie header must not be null");
        // Example: "PLAYER_TOKEN=abc-123; Path=/; HttpOnly; ..."
        String[] parts = setCookie.split(";", 2);
        String[] nameValue = parts[0].split("=", 2);
        String name = nameValue[0];
        String value = nameValue[1];
        return new Cookie(name, value);
    }

    // ---- WebSocket helpers ----

    private StompSession connect() throws Exception {
        if (stompClient == null) {
            stompClient = new WebSocketStompClient(new StandardWebSocketClient());
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        }

        String url = "ws://localhost:" + port + "/ws";

        CompletableFuture<StompSession> future =
                stompClient.connectAsync(url, new WebSocketHttpHeaders(), new StompSessionHandlerAdapter() {
                });

        return future.get(5, TimeUnit.SECONDS);
    }

    /**
     * Subscribe to the game state topic and return a future for the *next* GamePublicState.
     */
    private CompletableFuture<GamePublicState> awaitNextState(String gameId) {
        CompletableFuture<GamePublicState> future = new CompletableFuture<>();

        session.subscribe("/topic/games/" + gameId + "/state", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return GamePublicState.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (!future.isDone()) {
                    future.complete((GamePublicState) payload);
                }
            }
        });

        return future;
    }

    @AfterEach
    void cleanup() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        if (stompClient != null) {
            stompClient.stop();
        }
    }

    @Test
    void websocketReceivesGameStateOnJoinAndStart() throws Exception {
        // 1) Create game via HTTP (Host)
        String createPayload = """
                {
                  "playerName": "Host",
                  "totalRounds": 1
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").isString())
                .andExpect(jsonPath("$.gameCode").isString())
                .andExpect(jsonPath("$.playerId").isString())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Cookie hostCookie = extractPlayerCookie(createResult);

        JsonNode createJson = objectMapper.readTree(createResponse);
        String gameId = createJson.get("gameId").asText();
        String gameCode = createJson.get("gameCode").asText();

        // 2) Connect WebSocket
        session = connect();

        // 3) Subscribe and then trigger join (P2)
        CompletableFuture<GamePublicState> joinFuture = awaitNextState(gameId);

        String joinPayload = """
                {
                  "code": "%s",
                  "playerName": "P2"
                }
                """.formatted(gameCode);

        mockMvc.perform(post("/api/games/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinPayload))
                .andExpect(status().isOk());

        GamePublicState stateAfterJoin = joinFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(stateAfterJoin, "Should receive state after join via WebSocket");
        assertEquals(gameId, stateAfterJoin.getGameId());
        assertEquals(2, stateAfterJoin.getPlayers().size());
        assertEquals(GameStatus.LOBBY, stateAfterJoin.getGameStatus());

        // 4) Second join (P3) to satisfy min 3 players; WS assertion not needed here
        String joinPayload2 = """
                {
                  "code": "%s",
                  "playerName": "P3"
                }
                """.formatted(gameCode);

        mockMvc.perform(post("/api/games/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinPayload2))
                .andExpect(status().isOk());

        // 5) Subscribe again and then trigger start (hostCookie used for auth)
        CompletableFuture<GamePublicState> startFuture = awaitNextState(gameId);

        mockMvc.perform(post("/api/games/{gameId}/start", gameId)
                        .cookie(hostCookie))
                .andExpect(status().isOk());

        GamePublicState stateAfterStart = startFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(stateAfterStart, "Should receive state after start via WebSocket");
        assertEquals(GameStatus.IN_ROUND, stateAfterStart.getGameStatus());
        assertEquals(1, stateAfterStart.getCurrentRoundNumber());
        assertNotNull(stateAfterStart.getCurrentRoundStatus());
        assertEquals(3, stateAfterStart.getPlayers().size());
    }

    @Test
    void websocketReceivesFinalStateOnGuess() throws Exception {
        // 1) Create game (Host)
        String createPayload = """
                {
                  "playerName": "Host",
                  "totalRounds": 1
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isOk())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Cookie hostCookie = extractPlayerCookie(createResult);

        var createJson = objectMapper.readTree(createResponse);
        String gameId = createJson.get("gameId").asText();
        String gameCode = createJson.get("gameCode").asText();

        // 2) Two players join (P2, P3)
        String joinPayload1 = """
                { "code": "%s", "playerName": "P2" }
                """.formatted(gameCode);
        MvcResult join1Result = mockMvc.perform(post("/api/games/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinPayload1))
                .andExpect(status().isOk())
                .andReturn();
        Cookie p2Cookie = extractPlayerCookie(join1Result);
        String joinResp1 = join1Result.getResponse().getContentAsString();
        String p2Id = objectMapper.readTree(joinResp1).get("playerId").asText();

        String joinPayload2 = """
                { "code": "%s", "playerName": "P3" }
                """.formatted(gameCode);
        MvcResult join2Result = mockMvc.perform(post("/api/games/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinPayload2))
                .andExpect(status().isOk())
                .andReturn();
        Cookie p3Cookie = extractPlayerCookie(join2Result);
        String joinResp2 = join2Result.getResponse().getContentAsString();
        String p3Id = objectMapper.readTree(joinResp2).get("playerId").asText();

        // 3) Start game (hostCookie required)
        mockMvc.perform(post("/api/games/{gameId}/start", gameId)
                        .cookie(hostCookie))
                .andExpect(status().isOk());

        // 4) Connect WebSocket
        session = connect();

        // 5) Find Ramudu & Sita via /me endpoint, using each player's cookie
        var hostStateJson = mockMvc.perform(
                        get("/api/games/{gameId}/me", gameId)
                                .cookie(hostCookie))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var p2StateJson = mockMvc.perform(
                        get("/api/games/{gameId}/me", gameId)
                                .cookie(p2Cookie))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        var p3StateJson = mockMvc.perform(
                        get("/api/games/{gameId}/me", gameId)
                                .cookie(p3Cookie))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var hostState = objectMapper.readTree(hostStateJson);
        var p2State = objectMapper.readTree(p2StateJson);
        var p3State = objectMapper.readTree(p3StateJson);

        class PC {
            final String playerId;
            final String chit;
            final Cookie cookie;

            PC(String playerId, String chit, Cookie cookie) {
                this.playerId = playerId;
                this.chit = chit;
                this.cookie = cookie;
            }
        }

        PC pcHost = new PC(hostState.get("me").get("id").asText(),
                hostState.get("myChit").asText(),
                hostCookie);
        PC pcP2 = new PC(p2State.get("me").get("id").asText(),
                p2State.get("myChit").asText(),
                p2Cookie);
        PC pcP3 = new PC(p3State.get("me").get("id").asText(),
                p3State.get("myChit").asText(),
                p3Cookie);

        String ramuduId = null;
        String sitaId = null;
        Cookie ramuduCookie = null;

        for (PC pc : new PC[]{pcHost, pcP2, pcP3}) {
            if ("RAMUDU".equals(pc.chit)) {
                ramuduId = pc.playerId;
                ramuduCookie = pc.cookie;
            }
            if ("SITA".equals(pc.chit)) {
                sitaId = pc.playerId;
            }
        }

        assertNotNull(ramuduId);
        assertNotNull(sitaId);
        assertNotNull(ramuduCookie);

        // 6) Subscribe for state, then make correct guess (Ramudu must make the call)
        CompletableFuture<GamePublicState> finalFuture = awaitNextState(gameId);

        String guessPayload = """
                {
                  "guessedPlayerId": "%s"
                }
                """.formatted(sitaId);

        mockMvc.perform(post("/api/games/{gameId}/rounds/current/guess", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(guessPayload)
                        .cookie(ramuduCookie))
                .andExpect(status().isOk());

        GamePublicState finalState = finalFuture.get(5, TimeUnit.SECONDS);
        assertNotNull(finalState, "Should receive final game state via WebSocket");

        // First broadcast after guess is REVEAL (FINISHED is broadcast right after)
        assertEquals(GameStatus.REVEAL, finalState.getGameStatus());
        assertEquals(1, finalState.getCurrentRoundNumber());
        assertNotNull(finalState.getLastRoundScoreDelta());
        assertFalse(finalState.getLastRoundScoreDelta().isEmpty());
    }
}
