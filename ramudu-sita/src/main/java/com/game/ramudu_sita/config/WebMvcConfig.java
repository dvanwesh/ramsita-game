package com.game.ramudu_sita.config;

import com.game.ramudu_sita.rate.RateLimiterService;
import com.game.ramudu_sita.rate.RateLimitingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimiterService rateLimiterService;
    private final AppProperties appProperties;

    public WebMvcConfig(RateLimiterService rateLimiterService, AppProperties appProperties) {
        this.rateLimiterService = rateLimiterService;
        this.appProperties = appProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitingInterceptor(rateLimiterService))
                .addPathPatterns("/api/games/**");
    }

    /*@Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = appProperties.getAllowedOrigins().toArray(new String[0]);

        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }*/
}
