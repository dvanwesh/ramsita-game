package com.game.ramudu_sita.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsFilterConfig {

    private final AppProperties appProperties;

    public CorsFilterConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        // Use allowed-origins from application[-dev|-prod].yaml
        List<String> allowed = appProperties.getAllowedOrigins();
        // e.g. ["http://localhost:5173", ...]
        config.setAllowedOrigins(allowed);

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        // If you ever need front-end access to Set-Cookie, you can also:
        // config.addExposedHeader("Set-Cookie");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(0); // make sure this runs before other filters

        return bean;
    }
}
