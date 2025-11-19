package com.game.ramudu_sita.config;

import com.game.ramudu_sita.service.PlayerSessionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public PlayerSessionService playerSessionService() {
        return new PlayerSessionService();
    }
}
