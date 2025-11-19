package com.game.ramudu_sita.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    void fullFlow_singleRoundGame() throws Exception {
        // 1) Create game
        String createPayload = """
                {
                  "playerName": "Host",
                  "totalRounds": 1
                }
                """;

        String createResponse = mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").isString())
                .andExpect(jsonPath("$.gameCode").isString())
                .andExpect(jsonPath("$.playerId").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createJson = objectMapper.readTree(createResponse);
        String gameId = createJson.get("gameId").asText();
        String gameCode = createJson.get("gameCode").asText();
        String hostPlayerId = createJson.get("playerId").asText();

        // 2) Two players join
        String join1Payload = """
                {
                  "code": "%s",
                  "playerName": "P2"
                }
                """.formatted(gameCode);

        String join1Response = mockMvc.perform(post("/api/games/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(join1Payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String p2Id = objectMapper.readTree(join1Response).get("playerId").asText();

        String join2Payload = """
                {
                  "code": "%s",
                  "playerName": "P3"
                }
                """.formatted(gameCode);

        String join2Response = mockMvc.perform(post("/api/games/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(join2Payload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String p3Id = objectMapper.readTree(join2Response).get("playerId").asText();

        // 3) Start game
        mockMvc.perform(post("/api/games/{gameId}/start", gameId)
                        .param("playerId", hostPlayerId))
                .andExpect(status().isOk());

        // 4) Each player can see their chit
        String hostStateJsonStr = mockMvc.perform(get("/api/games/{gameId}/me", gameId)
                        .param("playerId", hostPlayerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameStatus", is("IN_ROUND")))
                .andExpect(jsonPath("$.currentRoundNumber", is(1)))
                .andExpect(jsonPath("$.roundStatus", is("WAITING_FOR_RAMUDU")))
                .andExpect(jsonPath("$.myChit", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String p2StateJsonStr = mockMvc.perform(get("/api/games/{gameId}/me", gameId)
                        .param("playerId", p2Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myChit", notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String p3StateJsonStr = mockMvc.perform(get("/api/games/{gameId}/me", gameId)
                        .param("playerId", p3Id))
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

            PlayerChit(String playerId, String chit) {
                this.playerId = playerId;
                this.chit = chit;
            }
        }

        PlayerChit pcHost = new PlayerChit(hostState.get("me").get("id").asText(),
                hostState.get("myChit").asText());
        PlayerChit pcP2 = new PlayerChit(p2State.get("me").get("id").asText(),
                p2State.get("myChit").asText());
        PlayerChit pcP3 = new PlayerChit(p3State.get("me").get("id").asText(),
                p3State.get("myChit").asText());

        PlayerChit[] pcs = {pcHost, pcP2, pcP3};

        String ramuduId = null;
        String sitaId = null;
        for (PlayerChit pc : pcs) {
            if ("RAMUDU".equals(pc.chit)) {
                ramuduId = pc.playerId;
            }
            if ("SITA".equals(pc.chit)) {
                sitaId = pc.playerId;
            }
        }

        assertNotNull(ramuduId, "Exactly one RAMUDU must exist");
        assertNotNull(sitaId, "Exactly one SITA must exist");

        // 5) Ramudu makes a correct guess
        String guessPayload = """
                {
                  "playerId": "%s",
                  "guessedPlayerId": "%s"
                }
                """.formatted(ramuduId, sitaId);

        mockMvc.perform(post("/api/games/{gameId}/rounds/current/guess", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(guessPayload))
                .andExpect(status().isOk());

        // 6) After single round game should be FINISHED; scores should be present
        mockMvc.perform(get("/api/games/{gameId}/me", gameId)
                        .param("playerId", hostPlayerId))
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

        String createResponse = mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode createJson = objectMapper.readTree(createResponse);
        String gameId = createJson.get("gameId").asText();
        String gameCode = createJson.get("gameCode").asText();

        String joinPayload = """
                {
                  "code": "%s",
                  "playerName": "P2"
                }
                """.formatted(gameCode);

        String joinResponse = mockMvc.perform(post("/api/games/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinPayload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String p2Id = objectMapper.readTree(joinResponse).get("playerId").asText();

        // When non-host tries to start, we expect 400 + error message
        mockMvc.perform(post("/api/games/{gameId}/start", gameId)
                        .param("playerId", p2Id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only host can start game"));
    }
}

