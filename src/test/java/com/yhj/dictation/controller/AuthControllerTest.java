package com.yhj.dictation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhj.dictation.dto.LoginRequest;
import com.yhj.dictation.dto.UserInfoDTO;
import com.yhj.dictation.entity.User;
import com.yhj.dictation.service.UserService;
import com.yhj.dictation.util.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

            mockMvc.perform(post("/api/v1/auth/login")
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

            mockMvc.perform(post("/api/v1/auth/login")
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

            mockMvc.perform(post("/api/v1/auth/login")
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

            mockMvc.perform(post("/api/v1/auth/login")
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
            mockMvc.perform(post("/api/v1/auth/logout"))
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

            mockMvc.perform(get("/api/v1/auth/avatars"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("getCurrentUser 方法测试")
    class GetCurrentUserTests {

        @Test
        @DisplayName("获取当前用户成功")
        void getCurrentUser_success() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn("USER");
                mockedUserContext.when(UserContext::getCurrentUserAvatar).thenReturn("avatar1.png");
                when(userService.getUserById(anyLong())).thenReturn(Optional.of(testUser));

                mockMvc.perform(get("/api/v1/auth/current"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.id").value(1))
                        .andExpect(jsonPath("$.data.username").value("testuser"));
            }
        }

        @Test
        @DisplayName("未登录 - userId为null")
        void getCurrentUser_notLoggedInUserIdNull() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(null);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");

                mockMvc.perform(get("/api/v1/auth/current"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("未登录"));
            }
        }

        @Test
        @DisplayName("未登录 - username为null")
        void getCurrentUser_notLoggedInUsernameNull() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn(null);

                mockMvc.perform(get("/api/v1/auth/current"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("未登录"));
            }
        }

        @Test
        @DisplayName("获取当前用户 - 用户不存在")
        void getCurrentUser_userNotFound() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn("USER");
                mockedUserContext.when(UserContext::getCurrentUserAvatar).thenReturn("avatar1.png");
                when(userService.getUserById(anyLong())).thenReturn(Optional.empty());

                mockMvc.perform(get("/api/v1/auth/current"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.username").value("testuser"));
                // status 字段不存在，因为用户不存在
            }
        }
    }

    @Nested
    @DisplayName("checkLoginStatus 方法测试")
    class CheckLoginStatusTests {

        @Test
        @DisplayName("已登录")
        void checkLoginStatus_loggedIn() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isLoggedIn).thenReturn(true);

                mockMvc.perform(get("/api/v1/auth/status"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data").value(true));
            }
        }

        @Test
        @DisplayName("未登录")
        void checkLoginStatus_notLoggedIn() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isLoggedIn).thenReturn(false);

                mockMvc.perform(get("/api/v1/auth/status"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data").value(false));
            }
        }
    }

    @Nested
    @DisplayName("login 边界条件测试")
    class LoginEdgeCasesTests {

        @Test
        @DisplayName("用户名为null")
        void login_nullUsername() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername(null);
            request.setPassword("password");

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("用户名不能为空"));
        }

        @Test
        @DisplayName("密码为null")
        void login_nullPassword() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword(null);

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("密码不能为空"));
        }

        @Test
        @DisplayName("用户名只有空格")
        void login_whitespaceUsername() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("   ");
            request.setPassword("password");

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("用户名不能为空"));
        }

        @Test
        @DisplayName("密码只有空格")
        void login_whitespacePassword() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setUsername("testuser");
            request.setPassword("   ");

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("密码不能为空"));
        }

        @Test
        @DisplayName("登录ADMIN角色用户")
        void login_adminUser() throws Exception {
            testUser.setRole(User.UserRole.ADMIN);
            LoginRequest request = new LoginRequest();
            request.setUsername("admin");
            request.setPassword("password");

            when(userService.login("admin", "password")).thenReturn(testUser);

            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.role").value("ADMIN"));
        }
    }
}