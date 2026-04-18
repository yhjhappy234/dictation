package com.yhj.dictation.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
        @DisplayName("创建 MD5 密码加密器")
        void passwordEncoder() {
            PasswordEncoder encoder = securityConfig.passwordEncoder();

            assertNotNull(encoder);
        }

        @Test
        @DisplayName("加密密码")
        void encodePassword() {
            PasswordEncoder encoder = securityConfig.passwordEncoder();

            String rawPassword = "123456";
            String encodedPassword = encoder.encode(rawPassword);

            assertNotNull(encodedPassword);
            assertEquals("e10adc3949ba59abbe56e057f20f883e", encodedPassword);
        }

        @Test
        @DisplayName("验证密码 - 正确")
        void matchesCorrectPassword() {
            PasswordEncoder encoder = securityConfig.passwordEncoder();

            String rawPassword = "123456";
            String encodedPassword = encoder.encode(rawPassword);

            assertTrue(encoder.matches(rawPassword, encodedPassword));
        }

        @Test
        @DisplayName("验证密码 - 错误")
        void matchesWrongPassword() {
            PasswordEncoder encoder = securityConfig.passwordEncoder();

            String rawPassword = "123456";
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