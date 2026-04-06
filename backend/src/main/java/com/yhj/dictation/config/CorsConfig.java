package com.yhj.dictation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:*}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        if ("*".equals(allowedOrigins)) {
            config.addAllowedOriginPattern("*");
        } else {
            Arrays.stream(allowedOrigins.split(","))
                    .forEach(config::addAllowedOrigin);
        }

        if ("*".equals(allowedMethods)) {
            config.addAllowedMethod("*");
        } else {
            Arrays.stream(allowedMethods.split(","))
                    .forEach(config::addAllowedMethod);
        }

        if ("*".equals(allowedHeaders)) {
            config.addAllowedHeader("*");
        } else {
            Arrays.stream(allowedHeaders.split(","))
                    .forEach(config::addAllowedHeader);
        }

        config.setAllowCredentials(allowCredentials);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
