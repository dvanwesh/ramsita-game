package com.game.ramudu_sita.api;


import com.game.ramudu_sita.api.dto.*;
import com.game.ramudu_sita.config.AppProperties;
import com.game.ramudu_sita.service.GameService;
import com.game.ramudu_sita.service.PlayerSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final PlayerSessionService playerSessionService;
    private final AppProperties appProperties;

    public GameController(GameService gameService, PlayerSessionService playerSessionService, AppProperties appProperties) {
        this.gameService = gameService;
        this.playerSessionService = playerSessionService;
        this.appProperties = appProperties;
    }

    @PostMapping
    public ResponseEntity<CreateOrJoinGameResponse> createGame(@RequestBody CreateGameRequest req, HttpServletRequest request, HttpServletResponse response) {
        String ipKey = extractClientIp(request);
        var result = gameService.createGame(req.getPlayerName(), req.getTotalRounds(), ipKey);
        var session = playerSessionService.createSession(result.gameId(), result.playerId());

        // Set HttpOnly cookie with player token
        ResponseCookie cookie = ResponseCookie.from("PLAYER_TOKEN", session.token())
                .httpOnly(true)
                .secure(appProperties.isCookieSecure()) // true in prod; false only for local HTTP
                .path("/")
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(
                new CreateOrJoinGameResponse(result.gameId(), result.code(), result.playerId(), session.token())
        );
    }

    private String extractClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/join")
    public ResponseEntity<CreateOrJoinGameResponse> joinGame(@RequestBody JoinGameRequest req, HttpServletResponse response) {
        var result = gameService.joinGame(req.getCode(), req.getPlayerName());
        var session = playerSessionService.createSession(result.gameId(), result.playerId());

        ResponseCookie cookie = ResponseCookie.from("PLAYER_TOKEN", session.token())
                .httpOnly(true)
                .secure(appProperties.isCookieSecure())
                .path("/")
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(
                new CreateOrJoinGameResponse(result.gameId(), result.code(), result.playerId(), session.token())
        );
    }

    @PostMapping("/{gameId}/start")
    public ResponseEntity<Void> startGame(
            @PathVariable String gameId,
            @CookieValue("PLAYER_TOKEN") String token
    ) {
        var session = playerSessionService.requireValidSession(token, gameId);
        gameService.startGame(gameId, session.playerId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{gameId}/me")
    public ResponseEntity<MyStateResponse> getMyState(
            @PathVariable String gameId,
            @CookieValue("PLAYER_TOKEN") String token
    ) {
        var session = playerSessionService.requireValidSession(token, gameId);
        return ResponseEntity.ok(gameService.getMyState(gameId, session.playerId()));
    }

    @PostMapping("/{gameId}/rounds/current/guess")
    public ResponseEntity<Void> makeGuess(
            @PathVariable String gameId,
            @CookieValue("PLAYER_TOKEN") String token,
            @RequestBody GuessRequest req
    ) {
        var session = playerSessionService.requireValidSession(token, gameId);
        gameService.makeGuess(gameId, session.playerId(), req.getGuessedPlayerId());
        return ResponseEntity.ok().build();
    }
}
