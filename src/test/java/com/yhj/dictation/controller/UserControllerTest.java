package com.yhj.dictation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhj.dictation.dto.UserCreateRequest;
import com.yhj.dictation.dto.UserUpdateRequest;
import com.yhj.dictation.dto.PasswordUpdateRequest;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
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
    @DisplayName("getAllUsers 方法测试")
    class GetAllUsersTests {

        @Test
        @DisplayName("管理员获取用户列表成功")
        void getAllUsers_admin() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(userService.getAllUsers()).thenReturn(List.of(testUser));

                mockMvc.perform(get("/api/users"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data").isArray());
            }
        }

        @Test
        @DisplayName("非管理员 - 权限不足")
        void getAllUsers_notAdmin() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(false);

                mockMvc.perform(get("/api/users"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("权限不足，只有管理员可以查看用户列表"));
            }
        }
    }

    @Nested
    @DisplayName("createUser 方法测试")
    class CreateUserTests {

        @Test
        @DisplayName("管理员创建用户成功")
        void createUser_admin() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            request.setUsername("newuser");
            request.setPassword("password");
            request.setRole("USER");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(userService.createUser(anyString(), anyString(), any())).thenReturn(testUser);

                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("非管理员 - 权限不足")
        void createUser_notAdmin() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            request.setUsername("newuser");
            request.setPassword("password");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(false);

                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("权限不足，只有管理员可以创建用户"));
            }
        }

        @Test
        @DisplayName("用户名已存在")
        void createUser_usernameExists() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            request.setUsername("existinguser");
            request.setPassword("password");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(userService.createUser(anyString(), anyString(), any()))
                        .thenThrow(new IllegalArgumentException("用户名已存在"));

                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false));
            }
        }
    }

    @Nested
    @DisplayName("updateMyPassword 方法测试")
    class UpdatePasswordTests {

        @Test
        @DisplayName("修改密码成功")
        void updateMyPassword_success() throws Exception {
            PasswordUpdateRequest request = new PasswordUpdateRequest();
            request.setNewPassword("newPassword123");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                testUser.setPassword("newEncodedPassword");
                when(userService.updatePassword(anyLong(), anyString())).thenReturn(testUser);

                mockMvc.perform(post("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("未登录")
        void updateMyPassword_notLoggedIn() throws Exception {
            PasswordUpdateRequest request = new PasswordUpdateRequest();
            request.setNewPassword("newPassword123");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(null);

                mockMvc.perform(post("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("未登录"));
            }
        }
    }

    @Nested
    @DisplayName("updateMyAvatar 方法测试")
    class UpdateAvatarTests {

        @Test
        @DisplayName("修改头像成功")
        void updateMyAvatar_success() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(() -> UserContext.updateAvatar(anyString())).thenAnswer(invocation -> null);
                when(userService.updateAvatar(anyLong(), anyString())).thenReturn(testUser);

                mockMvc.perform(post("/api/users/me/avatar?avatar=avatar2.png"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("未登录")
        void updateMyAvatar_notLoggedIn() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(null);

                mockMvc.perform(post("/api/users/me/avatar?avatar=avatar2.png"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("未登录"));
            }
        }
    }

    @Nested
    @DisplayName("isAdmin 方法测试")
    class IsAdminTests {

        @Test
        @DisplayName("是管理员")
        void isAdmin_true() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);

                mockMvc.perform(get("/api/users/me/is-admin"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data").value(true));
            }
        }

        @Test
        @DisplayName("不是管理员")
        void isAdmin_false() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(false);

                mockMvc.perform(get("/api/users/me/is-admin"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data").value(false));
            }
        }
    }

    @Nested
    @DisplayName("deleteUser 方法测试")
    class DeleteUserTests {

        @Test
        @DisplayName("管理员删除用户成功")
        void deleteUser_admin() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(2L); // 不是自己
                doNothing().when(userService).deleteUser(anyLong());

                mockMvc.perform(delete("/api/users/1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("不能删除自己")
        void deleteUser_cannotDeleteSelf() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L); // 是自己

                mockMvc.perform(delete("/api/users/1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("不能删除自己"));
            }
        }

        @Test
        @DisplayName("非管理员不能删除用户")
        void deleteUser_notAdmin() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(false);

                mockMvc.perform(delete("/api/users/1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false));
            }
        }

        @Test
        @DisplayName("删除用户失败")
        void deleteUser_fail() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(2L);
                doThrow(new IllegalArgumentException("用户不存在")).when(userService).deleteUser(anyLong());

                mockMvc.perform(delete("/api/users/999"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false));
            }
        }
    }

    @Nested
    @DisplayName("createUser 验证测试")
    class CreateUserValidationTests {

        @Test
        @DisplayName("用户名为空")
        void createUser_emptyUsername() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            request.setUsername("");
            request.setPassword("password");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);

                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("用户名不能为空"));
            }
        }

        @Test
        @DisplayName("密码为空")
        void createUser_emptyPassword() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            request.setUsername("newuser");
            request.setPassword("");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);

                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("密码不能为空"));
            }
        }

        @Test
        @DisplayName("无效角色")
        void createUser_invalidRole() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            request.setUsername("newuser");
            request.setPassword("password");
            request.setRole("INVALID_ROLE");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);

                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("无效的角色: INVALID_ROLE"));
            }
        }

        @Test
        @DisplayName("创建ADMIN角色用户")
        void createUser_adminRole() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            request.setUsername("newadmin");
            request.setPassword("password");
            request.setRole("ADMIN");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(userService.createUser(anyString(), anyString(), any())).thenReturn(testUser);

                mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }
    }

    @Nested
    @DisplayName("getUserById 方法测试")
    class GetUserByIdTests {

        @Test
        @DisplayName("管理员获取用户详情成功")
        void getUserById_admin() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(userService.getUserById(anyLong())).thenReturn(java.util.Optional.of(testUser));

                mockMvc.perform(get("/api/users/1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("非管理员不能获取用户详情")
        void getUserById_notAdmin() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(false);

                mockMvc.perform(get("/api/users/1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false));
            }
        }

        @Test
        @DisplayName("用户不存在")
        void getUserById_notFound() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(userService.getUserById(anyLong())).thenReturn(java.util.Optional.empty());

                mockMvc.perform(get("/api/users/999"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false));
            }
        }
    }

    @Nested
    @DisplayName("updateMyPassword 验证测试")
    class UpdatePasswordValidationTests {

        @Test
        @DisplayName("新密码为空")
        void updateMyPassword_emptyPassword() throws Exception {
            PasswordUpdateRequest request = new PasswordUpdateRequest();
            request.setNewPassword("");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);

                mockMvc.perform(post("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.message").value("新密码不能为空"));
            }
        }

        @Test
        @DisplayName("修改密码失败")
        void updateMyPassword_fail() throws Exception {
            PasswordUpdateRequest request = new PasswordUpdateRequest();
            request.setNewPassword("newPassword");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                when(userService.updatePassword(anyLong(), anyString())).thenThrow(new IllegalArgumentException("用户不存在"));

                mockMvc.perform(post("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false));
            }
        }
    }

    @Nested
    @DisplayName("updateMyAvatar 验证测试")
    class UpdateAvatarValidationTests {

        @Test
        @DisplayName("修改头像失败")
        void updateMyAvatar_fail() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                when(userService.updateAvatar(anyLong(), anyString())).thenThrow(new IllegalArgumentException("头像不存在"));

                mockMvc.perform(post("/api/users/me/avatar?avatar=invalid.png"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false));
            }
        }
    }

    @Nested
    @DisplayName("updateUser 方法测试")
    class UpdateUserTests {

        @Test
        @DisplayName("管理员更新用户成功 - 更新角色")
        void updateUser_role() throws Exception {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setRole("ADMIN");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(testUser));
                when(userService.updateRole(anyLong(), any())).thenReturn(testUser);

                mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("管理员更新用户成功 - 更新状态为ACTIVE")
        void updateUser_statusActive() throws Exception {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setStatus("ACTIVE");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(testUser));
                when(userService.enableUser(anyLong())).thenReturn(testUser);

                mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("管理员更新用户成功 - 更新状态为DISABLED")
        void updateUser_statusDisabled() throws Exception {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setStatus("DISABLED");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(testUser));
                when(userService.disableUser(anyLong())).thenReturn(testUser);

                mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("管理员更新用户成功 - 更新头像")
        void updateUser_avatar() throws Exception {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setAvatar("avatar2.png");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(testUser));
                when(userService.updateAvatar(anyLong(), anyString())).thenReturn(testUser);

                mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("管理员更新用户 - 用户不存在")
        void updateUser_userNotFound() throws Exception {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setRole("ADMIN");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(userService.getUserById(anyLong())).thenReturn(java.util.Optional.empty());

                mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false));
            }
        }

        @Test
        @DisplayName("非管理员不能更新用户")
        void updateUser_notAdmin() throws Exception {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setRole("ADMIN");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(false);

                mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false));
            }
        }

        @Test
        @DisplayName("无效角色")
        void updateUser_invalidRole() throws Exception {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setRole("INVALID_ROLE");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(testUser));

                mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false));
            }
        }

        @Test
        @DisplayName("无效状态")
        void updateUser_invalidStatus() throws Exception {
            UserUpdateRequest request = new UserUpdateRequest();
            request.setStatus("INVALID_STATUS");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(testUser));

                mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(false));
            }
        }

        @Test
        @DisplayName("空请求 - 不更新任何内容")
        void updateUser_emptyRequest() throws Exception {
            UserUpdateRequest request = new UserUpdateRequest();

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isAdmin).thenReturn(true);
                when(userService.getUserById(1L)).thenReturn(java.util.Optional.of(testUser));

                mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }
    }
}