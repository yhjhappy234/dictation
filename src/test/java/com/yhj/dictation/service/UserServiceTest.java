package com.yhj.dictation.service;

import com.yhj.dictation.entity.User;
import com.yhj.dictation.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
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
        void login_success() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

            User result = userService.login("testuser", "rawPassword");

            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
        }

        @Test
        @DisplayName("用户不存在")
        void login_userNotFound() {
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

            User result = userService.login("nonexistent", "password");

            assertNull(result);
        }

        @Test
        @DisplayName("密码错误")
        void login_wrongPassword() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            User result = userService.login("testuser", "wrongPassword");

            assertNull(result);
        }

        @Test
        @DisplayName("用户已禁用")
        void login_userDisabled() {
            testUser.setStatus(User.UserStatus.DISABLED);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            User result = userService.login("testuser", "password");

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("createUser 方法测试")
    class CreateUserTests {

        @Test
        @DisplayName("创建用户成功 - 默认角色")
        void createUser_success_defaultRole() {
            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.createUser("newuser", "password");

            assertNotNull(result);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("创建用户成功 - 指定角色")
        void createUser_success_withRole() {
            when(userRepository.existsByUsername("adminuser")).thenReturn(false);
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.createUser("adminuser", "password", User.UserRole.ADMIN);

            assertNotNull(result);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("用户名已存在")
        void createUser_usernameExists() {
            when(userRepository.existsByUsername("existinguser")).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () ->
                userService.createUser("existinguser", "password"));
        }
    }

    @Nested
    @DisplayName("getUserById 方法测试")
    class GetUserByIdTests {

        @Test
        @DisplayName("获取用户成功")
        void getUserById_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            Optional<User> result = userService.getUserById(1L);

            assertTrue(result.isPresent());
            assertEquals("testuser", result.get().getUsername());
        }

        @Test
        @DisplayName("用户不存在")
        void getUserById_notFound() {
            when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

            Optional<User> result = userService.getUserById(999L);

            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("updatePassword 方法测试")
    class UpdatePasswordTests {

        @Test
        @DisplayName("更新密码成功")
        void updatePassword_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.updatePassword(1L, "newPassword");

            assertNotNull(result);
            verify(passwordEncoder).encode("newPassword");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("用户不存在")
        void updatePassword_userNotFound() {
            when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                userService.updatePassword(999L, "newPassword"));
        }
    }

    @Nested
    @DisplayName("updateAvatar 方法测试")
    class UpdateAvatarTests {

        @Test
        @DisplayName("更新头像成功")
        void updateAvatar_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.updateAvatar(1L, "avatar2.png");

            assertNotNull(result);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("无效头像")
        void updateAvatar_invalidAvatar() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            assertThrows(IllegalArgumentException.class, () ->
                userService.updateAvatar(1L, "invalid.png"));
        }
    }

    @Nested
    @DisplayName("updateRole 方法测试")
    class UpdateRoleTests {

        @Test
        @DisplayName("更新角色成功")
        void updateRole_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.updateRole(1L, User.UserRole.ADMIN);

            assertNotNull(result);
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("disableUser/enableUser 方法测试")
    class StatusTests {

        @Test
        @DisplayName("禁用用户成功")
        void disableUser_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.disableUser(1L);

            assertNotNull(result);
            assertEquals(User.UserStatus.DISABLED, result.getStatus());
        }

        @Test
        @DisplayName("启用用户成功")
        void enableUser_success() {
            testUser.setStatus(User.UserStatus.DISABLED);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            User result = userService.enableUser(1L);

            assertNotNull(result);
            assertEquals(User.UserStatus.ACTIVE, result.getStatus());
        }
    }

    @Nested
    @DisplayName("deleteUser 方法测试")
    class DeleteUserTests {

        @Test
        @DisplayName("删除用户成功")
        void deleteUser_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            doNothing().when(userRepository).deleteById(1L);

            userService.deleteUser(1L);

            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("用户不存在")
        void deleteUser_notFound() {
            when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(IllegalArgumentException.class, () ->
                userService.deleteUser(999L));
        }
    }

    @Nested
    @DisplayName("其他方法测试")
    class OtherTests {

        @Test
        @DisplayName("获取所有用户")
        void getAllUsers_success() {
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            List<User> result = userService.getAllUsers();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("获取所有头像")
        void getAllAvatars_success() {
            List<String> avatars = userService.getAllAvatars();

            assertEquals(20, avatars.size());
        }

        @Test
        @DisplayName("获取随机头像")
        void getRandomAvatar_success() {
            String avatar = userService.getRandomAvatar();

            assertNotNull(avatar);
            assertTrue(avatar.startsWith("avatar"));
            assertTrue(avatar.endsWith(".png"));
        }

        @Test
        @DisplayName("检查是否是管理员 - 是")
        void isAdmin_true() {
            testUser.setRole(User.UserRole.ADMIN);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            boolean result = userService.isAdmin(1L);

            assertTrue(result);
        }

        @Test
        @DisplayName("检查是否是管理员 - 否")
        void isAdmin_false() {
            testUser.setRole(User.UserRole.USER);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            boolean result = userService.isAdmin(1L);

            assertFalse(result);
        }
    }
}