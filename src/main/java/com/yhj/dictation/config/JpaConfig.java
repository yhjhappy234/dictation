package com.yhj.dictation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA Repository 配置
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.yhj.dictation.repository")
public class JpaConfig {
}
