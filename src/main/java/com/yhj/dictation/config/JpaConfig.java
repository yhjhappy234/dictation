package com.yhj.dictation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA Repository 配置
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.yhj.dictation.repository")
public class JpaConfig {

    /**
     * 提供 ObjectMapper bean
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
