package com.yhj.dictation.interceptor;

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
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthInterceptor 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @InjectMocks
    private AuthInterceptor authInterceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        // 初始化设置
    }

    @Nested
    @DisplayName("preHandle 方法测试")
    class PreHandleTests {

        @Test
        @DisplayName("已登录 - 允许访问")
        void preHandle_loggedIn() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isLoggedIn).thenReturn(true);

                boolean result = authInterceptor.preHandle(request, response, null);

                assertTrue(result);
            }
        }

        @Test
        @DisplayName("未登录 - API请求返回JSON错误")
        void preHandle_notLoggedIn_apiRequest() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isLoggedIn).thenReturn(false);
                when(request.getRequestURI()).thenReturn("/api/some-endpoint");
                when(response.getWriter()).thenReturn(new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.ByteArrayOutputStream())));

                boolean result = authInterceptor.preHandle(request, response, null);

                assertFalse(result);
                verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                verify(response).setContentType("application/json;charset=UTF-8");
            }
        }

        @Test
        @DisplayName("未登录 - 页面请求重定向到登录页")
        void preHandle_notLoggedIn_pageRequest() throws Exception {
            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
                mockedUserContext.when(UserContext::isLoggedIn).thenReturn(false);
                when(request.getRequestURI()).thenReturn("/some-page");

                boolean result = authInterceptor.preHandle(request, response, null);

                assertFalse(result);
                verify(response).sendRedirect("/login");
            }
        }
    }
}