package com.yhj.dictation.config;

import com.yhj.dictation.interceptor.AuthInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * WebConfig 单元测试
 */
@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private AuthInterceptor authInterceptor;

    @Mock
    private InterceptorRegistry registry;

    @Mock
    private InterceptorRegistration registration;

    @InjectMocks
    private WebConfig webConfig;

    @Nested
    @DisplayName("addInterceptors 方法测试")
    class AddInterceptorsTests {

        @Test
        @DisplayName("注册拦截器成功")
        void addInterceptors_success() {
            // Mock registry.addInterceptor to return registration
            when(registry.addInterceptor(authInterceptor)).thenReturn(registration);
            when(registration.addPathPatterns(any(String[].class))).thenReturn(registration);
            when(registration.excludePathPatterns(any(String[].class))).thenReturn(registration);

            // 执行
            webConfig.addInterceptors(registry);

            // 验证拦截器被注册
            verify(registry).addInterceptor(authInterceptor);
            verify(registration).addPathPatterns(any(String[].class));
            verify(registration).excludePathPatterns(any(String[].class));
        }
    }
}