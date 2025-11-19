package com.game.ramudu_sita;

import com.game.ramudu_sita.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class RamuduSitaApplication {

	public static void main(String[] args) {
		SpringApplication.run(RamuduSitaApplication.class, args);
	}

}
