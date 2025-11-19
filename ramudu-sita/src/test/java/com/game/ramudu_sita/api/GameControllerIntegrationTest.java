package com.game.ramudu_sita.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.ramudu_sita.rate.RateLimiterService;
import com.game.ramudu_sita.service.GameService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameService gameService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @AfterEach
    void resetState() {
        // Clear all in-memory games
        gameService.clearAllForTests();

        // Reset rate limiter
        rateLimiterService.clearAllForTests();
    }

    /**
     * Helper to extract a Cookie object for PLAYER_TOKEN from the Set-Cookie header.
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

    @Test
    void fullFlow_singleRoundGame() throws Exception {
        // 1) Create game (host)
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

        JsonNode createJson = objectMapper.readTree(createResponse);
        String gameId = createJson.get("gameId").asText();
        String gameCode = createJson.get("gameCode").asText();

        // 2) Two players join
        String join1Payload = """
                {
                  "code": "%s",
                  "playerName": "P2"
                }
                """.formatted(gameCode);

        MvcResult join1Result = mockMvc.perform(post("/api/games/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(join1Payload))
                .andExpect(status().isOk())
                .andReturn();
        Cookie p2Cookie = extractPlayerCookie(join1Result);
        String join1Response = join1Result.getResponse().getContentAsString();
        String p2Id = objectMapper.readTree(join1Response).get("playerId").asText();

        String join2Payload = """
                {
                  "code": "%s",
                  "playerName": "P3"
                }
                """.formatted(gameCode);

        MvcResult join2Result = mockMvc.perform(post("/api/games/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(join2Payload))
                .andExpect(status().isOk())
                .andReturn();
        Cookie p3Cookie = extractPlayerCookie(join2Result);
        String join2Response = join2Result.getResponse().getContentAsString();
        String p3Id = objectMapper.readTree(join2Response).get("playerId").asText();

        // 3) Start game (host cookie must be used)
        mockMvc.perform(post("/api/games/{gameId}/start", gameId)
                        .cookie(hostCookie))
                .andExpect(status().isOk());

        // 4) Each player can see their chit using their own cookie
        String hostStateJsonStr = mockMvc.perform(get("/api/games/{gameId}/me", gameId)
                        .cookie(hostCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameStatus", is("IN_ROUND")))
                .andExpect(jsonPath("$.currentRoundNumber", is(1)))
                .andExpect(jsonPath("$.roundStatus", is("WAITING_FOR_RAMUDU")))
                .andExpect(jsonPath("$.myChit", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String p2StateJsonStr = mockMvc.perform(get("/api/games/{gameId}/me", gameId)
                        .cookie(p2Cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myChit", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String p3StateJsonStr = mockMvc.perform(get("/api/games/{gameId}/me", gameId)
                        .cookie(p3Cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myChit", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode hostState = objectMapper.readTree(hostStateJsonStr);
        JsonNode p2State = objectMapper.readTree(p2StateJsonStr);
        JsonNode p3State = objectMapper.readTree(p3StateJsonStr);

        // Find Ramudu & Sita
        class PlayerChit {
            final String playerId;
            final String chit;
            final Cookie cookie;

            PlayerChit(String playerId, String chit, Cookie cookie) {
                this.playerId = playerId;
                this.chit = chit;
                this.cookie = cookie;
            }
        }

        PlayerChit pcHost = new PlayerChit(
                hostState.get("me").get("id").asText(),
                hostState.get("myChit").asText(),
                hostCookie
        );
        PlayerChit pcP2 = new PlayerChit(
                p2State.get("me").get("id").asText(),
                p2State.get("myChit").asText(),
                p2Cookie
        );
        PlayerChit pcP3 = new PlayerChit(
                p3State.get("me").get("id").asText(),
                p3State.get("myChit").asText(),
                p3Cookie
        );

        PlayerChit[] pcs = {pcHost, pcP2, pcP3};

        String ramuduId = null;
        String sitaId = null;
        Cookie ramuduCookie = null;

        for (PlayerChit pc : pcs) {
            if ("RAMUDU".equals(pc.chit)) {
                ramuduId = pc.playerId;
                ramuduCookie = pc.cookie;
            }
            if ("SITA".equals(pc.chit)) {
                sitaId = pc.playerId;
            }
        }

        assertNotNull(ramuduId, "Exactly one RAMUDU must exist");
        assertNotNull(sitaId, "Exactly one SITA must exist");
        assertNotNull(ramuduCookie, "Ramudu cookie must be found");

        // 5) Ramudu makes a correct guess (payload must use Sita's playerId!)
        String guessPayload = """
                {
                  "guessedPlayerId": "%s"
                }
                """.formatted(sitaId);

        mockMvc.perform(post("/api/games/{gameId}/rounds/current/guess", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(guessPayload)
                        .cookie(ramuduCookie)) // must use Ramudu's session
                .andExpect(status().isOk());

        // 6) After single round game should be FINISHED; scores should be present
        mockMvc.perform(get("/api/games/{gameId}/me", gameId)
                        .cookie(hostCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameStatus", is("FINISHED")))
                .andExpect(jsonPath("$.players", hasSize(3)))
                .andExpect(jsonPath("$.players[*].totalScore", everyItem(greaterThanOrEqualTo(0))));
    }

    @Test
    void startGame_withNonHostViaHttpFails() throws Exception {
        // Create game & one joiner but don't start yet
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

        JsonNode createJson = objectMapper.readTree(createResponse);
        String gameId = createJson.get("gameId").asText();
        String gameCode = createJson.get("gameCode").asText();

        String joinPayload = """
                {
                  "code": "%s",
                  "playerName": "P2"
                }
                """.formatted(gameCode);

        MvcResult joinResult = mockMvc.perform(post("/api/games/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinPayload))
                .andExpect(status().isOk())
                .andReturn();
        Cookie p2Cookie = extractPlayerCookie(joinResult);

        // When non-host tries to start, we expect 400 + error message
        mockMvc.perform(post("/api/games/{gameId}/start", gameId)
                        .cookie(p2Cookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only host can start game"));
    }

    @Test
    void spamFilterBlocksTooManyActiveGamesPerCreator() throws Exception {

        // Create 5 games from same IP → allowed
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/games")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                { "playerName": "H", "totalRounds": 1 }
                                """))
                    .andExpect(status().isOk());
        }

        // 6th game should fail due to spam limit
        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "playerName": "H", "totalRounds": 1 }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("TOO_MANY_ACTIVE_GAMES")));
    }


}
