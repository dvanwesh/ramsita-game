package com.game.ramudu_sita.api;


import com.game.ramudu_sita.api.dto.*;
import com.game.ramudu_sita.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
@CrossOrigin // so your React app can call it during dev
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<CreateOrJoinGameResponse> createGame(@RequestBody CreateGameRequest req) {
        var result = gameService.createGame(req.getPlayerName(), req.getTotalRounds());
        return ResponseEntity.ok(
                new CreateOrJoinGameResponse(result.gameId(), result.code(), result.playerId())
        );
    }

    @PostMapping("/join")
    public ResponseEntity<CreateOrJoinGameResponse> joinGame(@RequestBody JoinGameRequest req) {
        var result = gameService.joinGame(req.getCode(), req.getPlayerName());
        return ResponseEntity.ok(
                new CreateOrJoinGameResponse(result.gameId(), result.code(), result.playerId())
        );
    }

    @PostMapping("/{gameId}/start")
    public ResponseEntity<Void> startGame(
            @PathVariable String gameId,
            @RequestParam String playerId
    ) {
        gameService.startGame(gameId, playerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{gameId}/me")
    public ResponseEntity<MyStateResponse> getMyState(
            @PathVariable String gameId,
            @RequestParam String playerId
    ) {
        return ResponseEntity.ok(gameService.getMyState(gameId, playerId));
    }

    @PostMapping("/{gameId}/rounds/current/guess")
    public ResponseEntity<Void> makeGuess(
            @PathVariable String gameId,
            @RequestBody GuessRequest req
    ) {
        gameService.makeGuess(gameId, req.getPlayerId(), req.getGuessedPlayerId());
        return ResponseEntity.ok().build();
    }
}
