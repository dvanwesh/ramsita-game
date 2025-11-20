package com.game.ramudu_sita.service;

import com.game.ramudu_sita.api.dto.GamePublicState;
import com.game.ramudu_sita.api.dto.MyStateResponse;
import com.game.ramudu_sita.model.*;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private static final long IDLE_TTL_MINUTES = 60;     // active but idle too long
    private static final long FINISHED_TTL_MINUTES = 30; // finished games kept shorter

    private final Map<String, GameState> gamesById = new ConcurrentHashMap<>();
    private final Map<String, String> gameIdByCode = new ConcurrentHashMap<>();

    private final SecureRandom random = new SecureRandom();
    private final ChitType[] CHITS = {
            ChitType.RAMUDU,
            ChitType.SITA,
            ChitType.BHARATA,
            ChitType.SHATRUGHNA,
            ChitType.HANUMAN
    };

    private final SimpMessagingTemplate messagingTemplate;

    public GameService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // --- Game lifecycle ---

    public CreateResult createGame(String playerName, int totalRounds, String creatorKey) {
        // Spam protection: limit active games per creator
        long activeGames = gamesById.values().stream()
                .filter(g -> creatorKey.equals(g.getCreatorKey()))
                .filter(g -> g.getStatus() != GameStatus.FINISHED)
                .count();

        if (activeGames >= 5) { // e.g. max 5 active games per IP
            throw new IllegalStateException("Too many active games for this client");
        }

        String gameId = UUID.randomUUID().toString();
        String code = generateCode();

        GameState game = new GameState(gameId, code, totalRounds);
        game.setCreatorKey(creatorKey);
        Player host = new Player(UUID.randomUUID().toString(), playerName, true);
        game.getPlayers().put(host.getId(), host);

        gamesById.put(gameId, game);
        gameIdByCode.put(code, gameId);

        broadcastGameState(game);
        return new CreateResult(gameId, code, host.getId());
    }

    public CreateResult joinGame(String code, String playerName) {
        GameState game = findGameByCode(code);
        if (game.getStatus() != GameStatus.LOBBY) {
            throw new IllegalStateException("Game already started");
        }
        Player player = new Player(UUID.randomUUID().toString(), playerName, false);
        game.getPlayers().put(player.getId(), player);

        broadcastGameState(game);
        return new CreateResult(game.getId(), game.getCode(), player.getId());
    }

    public void startGame(String gameId, String requestingPlayerId) {
        GameState game = findGameById(gameId);

        Player requester = game.getPlayers().get(requestingPlayerId);
        if (requester == null || !requester.isHost()) {
            throw new IllegalStateException("Only host can start game");
        }

        if (game.getPlayers().size() < 3) {
            throw new IllegalStateException("Need at least 3 players");
        }

        if (game.getStatus() != GameStatus.LOBBY) {
            throw new IllegalStateException("Game already started");
        }

        game.setCurrentRoundNumber(1);
        game.setStatus(GameStatus.IN_ROUND);

        RoundState round = new RoundState(1);
        game.getRounds().put(1, round);

        distributeChits(game, round); // also broadcasts
    }

    // --- Round actions ---

    public void makeGuess(String gameId, String playerId, String guessedPlayerId) {
        GameState game = findGameById(gameId);
        RoundState round = game.getCurrentRound();

        if (round == null) {
            throw new IllegalStateException("No active round");
        }
        if (round.getStatus() != RoundStatus.WAITING_FOR_RAMUDU) {
            throw new IllegalStateException("Not expecting a guess now");
        }

        if (!Objects.equals(round.getRamuduPlayerId(), playerId)) {
            throw new IllegalStateException("Only Ramudu can guess");
        }

        round.setGuessTargetPlayerId(guessedPlayerId);

        // compute scores
        Map<String, Integer> delta = computeScores(round);
        round.setScoreDelta(delta);

        // apply to players
        for (Map.Entry<String, Integer> entry : delta.entrySet()) {
            Player p = game.getPlayers().get(entry.getKey());
            if (p != null) {
                p.addScore(entry.getValue());
            }
        }

        round.setStatus(RoundStatus.COMPLETED);
        game.setStatus(GameStatus.REVEAL);

        broadcastGameState(game); // show reveal + score delta

        // move to next round if any
        int current = game.getCurrentRoundNumber();
        if (current < game.getTotalRounds()) {
            int next = current + 1;
            game.setCurrentRoundNumber(next);

            RoundState nextRound = new RoundState(next);
            game.getRounds().put(next, nextRound);
            game.setStatus(GameStatus.IN_ROUND);
            distributeChits(game, nextRound); // also broadcasts
        } else {
            game.setStatus(GameStatus.FINISHED);
            broadcastGameState(game);
        }
    }

    // --- Queries ---

    public MyStateResponse getMyState(String gameId, String playerId) {
        GameState game = findGameById(gameId);
        Player me = game.getPlayers().get(playerId);
        if (me == null) {
            throw new IllegalArgumentException("Player not in game");
        }

        MyStateResponse resp = new MyStateResponse();
        resp.setGameId(game.getId());
        resp.setGameCode(game.getCode());
        resp.setGameStatus(game.getStatus());
        resp.setTotalRounds(game.getTotalRounds());
        resp.setCurrentRoundNumber(game.getCurrentRoundNumber());

        MyStateResponse.PlayerView meView = new MyStateResponse.PlayerView(
                me.getId(), me.getName(), me.isHost(), me.getTotalScore()
        );
        resp.setMe(meView);

        List<MyStateResponse.PlayerView> all =
                game.getPlayers().values().stream()
                        .map(p -> new MyStateResponse.PlayerView(
                                p.getId(), p.getName(), p.isHost(), p.getTotalScore()))
                        .toList();
        resp.setPlayers(all);

        RoundState currentRound = game.getCurrentRound();
        if (currentRound != null) {
            resp.setRoundStatus(currentRound.getStatus());
            resp.setMyChit(currentRound.getAssignments().get(playerId));

            // last completed round score delta (can be from current or previous)
            resp.setLastRoundScoreDelta(currentRound.getScoreDelta());
        }

        return resp;
    }

    // --- Helpers ---

    private GameState findGameById(String gameId) {
        GameState game = gamesById.get(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }
        return game;
    }

    private GameState findGameByCode(String code) {
        String id = gameIdByCode.get(code);
        if (id == null) {
            throw new IllegalArgumentException("Game not found with code: " + code);
        }
        return findGameById(id);
    }

    private String generateCode() {
        String letters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(letters.charAt(random.nextInt(letters.length())));
        }
        String code = sb.toString();
        if (gameIdByCode.containsKey(code)) {
            return generateCode();
        }
        return code;
    }

    private void distributeChits(GameState game, RoundState round) {
        List<Player> players = new ArrayList<>(game.getPlayers().values());

        // ensure chit count == player count
        if (players.size() > CHITS.length) {
            throw new IllegalStateException("Not enough chits for players in this simple demo");
        }

        List<ChitType> available = new ArrayList<>(Arrays.asList(CHITS).subList(0, players.size()));
        Collections.shuffle(available, random);

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            ChitType chit = available.get(i);
            round.getAssignments().put(p.getId(), chit);

            if (chit.isRamudu()) {
                round.setRamuduPlayerId(p.getId());
            }
            if (chit.isSita()) {
                round.setSitaPlayerId(p.getId());
            }
        }

        round.setStatus(RoundStatus.WAITING_FOR_RAMUDU);
        broadcastGameState(game);
    }

    private Map<String, Integer> computeScores(RoundState round) {
        Map<String, Integer> delta = new HashMap<>();

        // Everyone gets their base chit points initially
        for (Map.Entry<String, ChitType> e : round.getAssignments().entrySet()) {
            String playerId = e.getKey();
            ChitType chit = e.getValue();
            delta.put(playerId, chit.getBasePoints());
        }

        String sita = round.getSitaPlayerId();
        String ramudu = round.getRamuduPlayerId();
        String guessTarget = round.getGuessTargetPlayerId();

        // apply Ramudu special rule
        if (guessTarget != null && guessTarget.equals(sita)) {
            delta.put(ramudu, 5000);
        } else {
            delta.put(ramudu, 0);
            // Sita gets 5000 points
            delta.put(sita, 5000);
        }

        return delta;
    }

    // --- WebSocket broadcast ---

    private void broadcastGameState(GameState game) {
        GamePublicState dto = toPublicState(game);
        String destination = "/topic/games/" + game.getId() + "/state";
        messagingTemplate.convertAndSend(destination, dto);
        game.touch();
    }

    private GamePublicState toPublicState(GameState game) {
        GamePublicState state = new GamePublicState();
        state.setGameId(game.getId());
        state.setGameCode(game.getCode());
        state.setGameStatus(game.getStatus());
        state.setTotalRounds(game.getTotalRounds());
        state.setCurrentRoundNumber(game.getCurrentRoundNumber());

        List<GamePublicState.PlayerSummary> players =
                game.getPlayers().values().stream()
                        .map(p -> new GamePublicState.PlayerSummary(
                                p.getId(), p.getName(), p.isHost(), p.getTotalScore()
                        ))
                        .toList();
        state.setPlayers(players);

        RoundState currentRound = game.getCurrentRound();
        if (currentRound != null) {
            state.setCurrentRoundStatus(currentRound.getStatus());
            state.setLastRoundScoreDelta(currentRound.getScoreDelta());
        }

        return state;
    }

    /**
     * Remove old games from memory to avoid unbounded growth.
     * Intended to be called periodically (e.g. via @Scheduled).
     */
    public void cleanupOldGames() {
        Instant now = Instant.now();

        List<String> toRemove = new ArrayList<>();

        for (GameState game : gamesById.values()) {
            Instant last = game.getLastActivityAt();
            if (last == null) {
                // Shouldn't happen, but be safe
                continue;
            }

            long idleMinutes = Duration.between(last, now).toMinutes();

            boolean remove = false;
            if (game.getStatus() == GameStatus.FINISHED) {
                if (idleMinutes >= FINISHED_TTL_MINUTES) {
                    remove = true;
                }
            } else {
                if (idleMinutes >= IDLE_TTL_MINUTES) {
                    remove = true;
                }
            }

            if (remove) {
                toRemove.add(game.getId());
            }
        }

        for (String gameId : toRemove) {
            GameState removed = gamesById.remove(gameId);
            if (removed != null) {
                gameIdByCode.remove(removed.getCode());
            }
        }
    }

    public GameState getGame(String gameId) {
        return gamesById.get(gameId);
    }

    @Profile("test")
    public void clearAllForTests() {
        gamesById.clear();
        gameIdByCode.clear();
    }

    // --- helper DTO for create/join result ---

    public record CreateResult(String gameId, String code, String playerId) {
    }
}

