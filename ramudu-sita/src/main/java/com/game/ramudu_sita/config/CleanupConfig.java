package com.game.ramudu_sita.config;

import com.game.ramudu_sita.service.GameService;
import com.game.ramudu_sita.service.PlayerSessionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@Configuration
public class CleanupConfig {

    private final GameService gameService;
    private final PlayerSessionService playerSessionService;

    public CleanupConfig(GameService gameService, PlayerSessionService playerSessionService) {
        this.gameService = gameService;
        this.playerSessionService = playerSessionService;
    }

    @Scheduled(fixedDelay = 600_000) // every 10 minutes
    public void cleanup() {
        gameService.cleanupOldGames();
        playerSessionService.cleanupExpiredSessions();
    }
}
