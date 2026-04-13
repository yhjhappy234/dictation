package com.yhj.dictation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置
 * 禁用默认的安全机制，仅使用 BCrypt 密码加密
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * BCrypt 密码加密器
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链配置
     * 禁用所有默认安全机制，因为我们使用自定义的 Session 认证
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（前后端分离场景）
                .csrf(AbstractHttpConfigurer::disable)
                // 允许所有请求（不使用 Spring Security 的认证）
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // 禁用表单登录
                .formLogin(AbstractHttpConfigurer::disable)
                // 禁用 HTTP Basic 认证
                .httpBasic(AbstractHttpConfigurer::disable)
                // 禁用登出处理（使用自定义登出）
                .logout(AbstractHttpConfigurer::disable)
                // 禁用 Session 管理（使用自定义 Session）
                .sessionManagement(session -> session.disable());

        return http.build();
    }
}