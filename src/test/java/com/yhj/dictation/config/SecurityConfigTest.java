package com.yhj.dictation.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityConfig 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;

    @Nested
    @DisplayName("passwordEncoder 方法测试")
    class PasswordEncoderTests {

        @Test
        @DisplayName("创建 BCrypt 密码加密器")
        void passwordEncoder() {
            BCryptPasswordEncoder encoder = securityConfig.passwordEncoder();

            assertNotNull(encoder);
            assertTrue(encoder instanceof BCryptPasswordEncoder);
        }

        @Test
        @DisplayName("加密密码")
        void encodePassword() {
            BCryptPasswordEncoder encoder = securityConfig.passwordEncoder();

            String rawPassword = "testPassword";
            String encodedPassword = encoder.encode(rawPassword);

            assertNotNull(encodedPassword);
            assertTrue(encoder.matches(rawPassword, encodedPassword));
        }

        @Test
        @DisplayName("验证密码 - 正确")
        void matchesCorrectPassword() {
            BCryptPasswordEncoder encoder = securityConfig.passwordEncoder();

            String rawPassword = "testPassword";
            String encodedPassword = encoder.encode(rawPassword);

            assertTrue(encoder.matches(rawPassword, encodedPassword));
        }

        @Test
        @DisplayName("验证密码 - 错误")
        void matchesWrongPassword() {
            BCryptPasswordEncoder encoder = securityConfig.passwordEncoder();

            String rawPassword = "testPassword";
            String encodedPassword = encoder.encode(rawPassword);

            assertFalse(encoder.matches("wrongPassword", encodedPassword));
        }
    }

    @Nested
    @DisplayName("securityFilterChain 方法测试")
    class SecurityFilterChainTests {

        @Test
        @DisplayName("SecurityConfig 实例创建")
        void createInstance() {
            SecurityConfig config = new SecurityConfig();
            assertNotNull(config);
        }
    }
}