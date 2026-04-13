package com.yhj.dictation.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UserContext 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserContextTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private ServletRequestAttributes attributes;

    @Nested
    @DisplayName("getCurrentUserId 方法测试")
    class GetCurrentUserIdTests {

        @Test
        @DisplayName("获取用户ID成功")
        void getCurrentUserId_success() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);
                when(session.getAttribute("userId")).thenReturn(1L);

                Long userId = UserContext.getCurrentUserId();

                assertEquals(1L, userId);
            }
        }

        @Test
        @DisplayName("用户ID为null")
        void getCurrentUserId_null() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);
                when(session.getAttribute("userId")).thenReturn(null);

                Long userId = UserContext.getCurrentUserId();

                assertNull(userId);
            }
        }

        @Test
        @DisplayName("无Session")
        void getCurrentUserId_noSession() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(null);

                Long userId = UserContext.getCurrentUserId();

                assertNull(userId);
            }
        }

        @Test
        @DisplayName("无RequestAttributes")
        void getCurrentUserId_noAttributes() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                Long userId = UserContext.getCurrentUserId();

                assertNull(userId);
            }
        }
    }

    @Nested
    @DisplayName("getCurrentUsername 方法测试")
    class GetCurrentUsernameTests {

        @Test
        @DisplayName("获取用户名成功")
        void getCurrentUsername_success() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);
                when(session.getAttribute("username")).thenReturn("testuser");

                String username = UserContext.getCurrentUsername();

                assertEquals("testuser", username);
            }
        }

        @Test
        @DisplayName("用户名不是String类型")
        void getCurrentUsername_notString() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);
                when(session.getAttribute("username")).thenReturn(123); // 返回Integer而非String

                String username = UserContext.getCurrentUsername();

                assertNull(username);
            }
        }

        @Test
        @DisplayName("无Session")
        void getCurrentUsername_noSession() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(null);

                String username = UserContext.getCurrentUsername();

                assertNull(username);
            }
        }
    }

    @Nested
    @DisplayName("getCurrentUserRole 方法测试")
    class GetCurrentUserRoleTests {

        @Test
        @DisplayName("获取用户角色成功")
        void getCurrentUserRole_success() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);
                when(session.getAttribute("userRole")).thenReturn("ADMIN");

                String role = UserContext.getCurrentUserRole();

                assertEquals("ADMIN", role);
            }
        }

        @Test
        @DisplayName("角色不是String类型")
        void getCurrentUserRole_notString() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);
                when(session.getAttribute("userRole")).thenReturn(123);

                String role = UserContext.getCurrentUserRole();

                assertNull(role);
            }
        }

        @Test
        @DisplayName("无Session")
        void getCurrentUserRole_noSession() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(null);

                String role = UserContext.getCurrentUserRole();

                assertNull(role);
            }
        }
    }

    @Nested
    @DisplayName("getCurrentUserAvatar 方法测试")
    class GetCurrentUserAvatarTests {

        @Test
        @DisplayName("获取用户头像成功")
        void getCurrentUserAvatar_success() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);
                when(session.getAttribute("userAvatar")).thenReturn("avatar1.png");

                String avatar = UserContext.getCurrentUserAvatar();

                assertEquals("avatar1.png", avatar);
            }
        }

        @Test
        @DisplayName("头像不是String类型")
        void getCurrentUserAvatar_notString() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);
                when(session.getAttribute("userAvatar")).thenReturn(123);

                String avatar = UserContext.getCurrentUserAvatar();

                assertNull(avatar);
            }
        }

        @Test
        @DisplayName("无Session")
        void getCurrentUserAvatar_noSession() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(null);

                String avatar = UserContext.getCurrentUserAvatar();

                assertNull(avatar);
            }
        }
    }

    @Nested
    @DisplayName("isAdmin 方法测试")
    class IsAdminTests {

        @Test
        @DisplayName("是管理员")
        void isAdmin_true() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);
                when(session.getAttribute("userRole")).thenReturn("ADMIN");

                assertTrue(UserContext.isAdmin());
            }
        }

        @Test
        @DisplayName("不是管理员")
        void isAdmin_false() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);
                when(session.getAttribute("userRole")).thenReturn("USER");

                assertFalse(UserContext.isAdmin());
            }
        }

        @Test
        @DisplayName("角色为null")
        void isAdmin_null() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);
                when(session.getAttribute("userRole")).thenReturn(null);

                assertFalse(UserContext.isAdmin());
            }
        }
    }

    @Nested
    @DisplayName("setCurrentUser 方法测试")
    class SetCurrentUserTests {

        @Test
        @DisplayName("设置用户信息成功")
        void setCurrentUser_success() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);

                UserContext.setCurrentUser(1L, "testuser", "USER", "avatar1.png");

                verify(session).setAttribute("userId", 1L);
                verify(session).setAttribute("username", "testuser");
                verify(session).setAttribute("userRole", "USER");
                verify(session).setAttribute("userAvatar", "avatar1.png");
            }
        }

        @Test
        @DisplayName("无Session时不设置")
        void setCurrentUser_noSession() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(null);

                UserContext.setCurrentUser(1L, "testuser", "USER", "avatar1.png");

                // 不调用session的setAttribute方法
                verify(session, never()).setAttribute(any(), any());
            }
        }
    }

    @Nested
    @DisplayName("updateAvatar 方法测试")
    class UpdateAvatarTests {

        @Test
        @DisplayName("更新头像成功")
        void updateAvatar_success() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);

                UserContext.updateAvatar("avatar2.png");

                verify(session).setAttribute("userAvatar", "avatar2.png");
            }
        }

        @Test
        @DisplayName("无Session时不更新")
        void updateAvatar_noSession() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(null);

                UserContext.updateAvatar("avatar2.png");

                verify(session, never()).setAttribute(any(), any());
            }
        }
    }

    @Nested
    @DisplayName("clearCurrentUser 方法测试")
    class ClearCurrentUserTests {

        @Test
        @DisplayName("清除用户信息成功")
        void clearCurrentUser_success() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);

                UserContext.clearCurrentUser();

                verify(session).removeAttribute("userId");
                verify(session).removeAttribute("username");
                verify(session).removeAttribute("userRole");
                verify(session).removeAttribute("userAvatar");
                verify(session).invalidate();
            }
        }

        @Test
        @DisplayName("无Session时不清除")
        void clearCurrentUser_noSession() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(null);

                UserContext.clearCurrentUser();

                verify(session, never()).removeAttribute(any());
                verify(session, never()).invalidate();
            }
        }
    }

    @Nested
    @DisplayName("isLoggedIn 方法测试")
    class IsLoggedInTests {

        @Test
        @DisplayName("已登录")
        void isLoggedIn_true() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);
                when(session.getAttribute("userId")).thenReturn(1L);
                when(session.getAttribute("username")).thenReturn("testuser");

                assertTrue(UserContext.isLoggedIn());
            }
        }

        @Test
        @DisplayName("未登录 - userId为null")
        void isLoggedIn_userIdNull() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);
                when(session.getAttribute("userId")).thenReturn(null);

                assertFalse(UserContext.isLoggedIn());
            }
        }

        @Test
        @DisplayName("未登录 - username为null")
        void isLoggedIn_usernameNull() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);
                when(request.getSession(false)).thenReturn(session);
                when(session.getAttribute("userId")).thenReturn(1L);
                when(session.getAttribute("username")).thenReturn(null);

                assertFalse(UserContext.isLoggedIn());
            }
        }
    }

    @Nested
    @DisplayName("getCurrentRequest 方法测试")
    class GetCurrentRequestTests {

        @Test
        @DisplayName("获取当前请求成功")
        void getCurrentRequest_success() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);
                when(attributes.getRequest()).thenReturn(request);

                HttpServletRequest result = UserContext.getCurrentRequest();

                assertEquals(request, result);
            }
        }

        @Test
        @DisplayName("无RequestAttributes")
        void getCurrentRequest_null() {
            try (MockedStatic<RequestContextHolder> mockedContext = mockStatic(RequestContextHolder.class)) {
                mockedContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                HttpServletRequest result = UserContext.getCurrentRequest();

                assertNull(result);
            }
        }
    }
}