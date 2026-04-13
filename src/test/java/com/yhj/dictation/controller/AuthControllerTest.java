package com.yhj.dictation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhj.dictation.dto.LoginRequest;
import com.yhj.dictation.dto.UserInfoDTO;
import com.yhj.dictation.entity.User;
import com.yhj.dictation.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setAvatar("avatar1.png");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("login 方法测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功")
        void login_success() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("password");

            when(userService.login("testuser", "password")).thenReturn(testUser);

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("登录成功"));
        }

        @Test
        @DisplayName("用户名为空")
        void login_emptyUsername() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("");
            request.setPassword("password");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("用户名不能为空"));
        }

        @Test
        @DisplayName("密码为空")
        void login_emptyPassword() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("密码不能为空"));
        }

        @Test
        @DisplayName("登录失败 - 用户名或密码错误")
        void login_failed() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("wrongpassword");

            when(userService.login(anyString(), anyString())).thenReturn(null);

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("用户名或密码错误"));
        }
    }

    @Nested
    @DisplayName("logout 方法测试")
    class LogoutTests {

        @Test
        @DisplayName("登出成功")
        void logout_success() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("登出成功"));
        }
    }

    @Nested
    @DisplayName("getAllAvatars 方法测试")
    class AvatarTests {

        @Test
        @DisplayName("获取头像列表")
        void getAllAvatars_success() throws Exception {
            when(userService.getAllAvatars()).thenReturn(java.util.List.of("avatar1.png", "avatar2.png"));

            mockMvc.perform(get("/api/auth/avatars"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}