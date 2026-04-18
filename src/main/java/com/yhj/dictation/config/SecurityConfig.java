package com.yhj.dictation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Spring Security 配置
 * 禁用默认的安全机制，使用 MD5 密码加密
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * MD5 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return md5Encode(rawPassword.toString());
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return md5Encode(rawPassword.toString()).equals(encodedPassword);
            }

            private String md5Encode(String input) {
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] digest = md.digest(input.getBytes());
                    StringBuilder sb = new StringBuilder();
                    for (byte b : digest) {
                        sb.append(String.format("%02x", b));
                    }
                    return sb.toString();
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("MD5算法不存在", e);
                }
            }
        };
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
                // 置用登出处理（使用自定义登出）
                .logout(AbstractHttpConfigurer::disable)
                // 禁用 Spring Security 的 Session 管理，但保留 Session 功能
                .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS));

        return http.build();
    }
}