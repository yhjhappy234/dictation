package com.yhj.dictation.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhj.dictation.annotation.AuditLog;
import com.yhj.dictation.entity.AuditLogEntity;
import com.yhj.dictation.service.AuditLogService;
import com.yhj.dictation.util.UserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuditAspect 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    @Mock
    private Method method;

    @Mock
    private AuditLog auditLogAnnotation;

    @InjectMocks
    private AuditAspect auditAspect;

    @BeforeEach
    void setUp() {
        // 默认设置
        lenient().when(auditLogAnnotation.operation()).thenReturn("测试操作");
        lenient().when(auditLogAnnotation.recordParams()).thenReturn(true);
        lenient().when(auditLogAnnotation.recordResult()).thenReturn(false);
    }

    @Nested
    @DisplayName("around 方法测试")
    class AroundTests {

        @Test
        @DisplayName("方法执行成功 - 记录审计日志")
        void around_success() throws Throwable {
            // 设置 mock
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{"param1"});
            when(joinPoint.proceed()).thenReturn("result");
            when(objectMapper.writeValueAsString(any())).thenReturn("[\"param1\"]");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                // 执行
                Object result = auditAspect.around(joinPoint);

                // 验证
                assertEquals("result", result);
                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("方法执行失败 - 记录错误日志")
        void around_failure() throws Throwable {
            // 设置 mock
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(joinPoint.proceed()).thenThrow(new RuntimeException("测试异常"));

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                // 执行并验证抛出异常
                assertThrows(RuntimeException.class, () -> auditAspect.around(joinPoint));

                // 验证审计日志被保存
                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("未登录用户 - 使用 anonymous")
        void around_notLoggedIn() throws Throwable {
            // 设置 mock
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            lenient().when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(joinPoint.proceed()).thenReturn("result");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(null);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn(null);
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                // 执行
                Object result = auditAspect.around(joinPoint);

                // 验证
                assertEquals("result", result);
                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("不记录参数")
        void around_noRecordParams() throws Throwable {
            // 设置不记录参数
            when(auditLogAnnotation.recordParams()).thenReturn(false);

            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.proceed()).thenReturn("result");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                // 执行
                auditAspect.around(joinPoint);

                // 验证不调用 objectMapper 序列化参数
                verify(objectMapper, never()).writeValueAsString(any());
            }
        }

        @Test
        @DisplayName("参数序列化失败")
        void around_paramsSerializationFailed() throws Throwable {
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{"param"});
            when(joinPoint.proceed()).thenReturn("result");
            when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("序列化失败"));

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                // 执行
                auditAspect.around(joinPoint);

                // 验证审计日志仍然被保存
                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("记录返回结果")
        void around_recordResult() throws Throwable {
            when(auditLogAnnotation.recordResult()).thenReturn(true);
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(joinPoint.proceed()).thenReturn("resultValue");
            when(objectMapper.writeValueAsString(any())).thenReturn("\"resultValue\"");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                auditAspect.around(joinPoint);

                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("结果序列化失败")
        void around_resultSerializationFailed() throws Throwable {
            when(auditLogAnnotation.recordResult()).thenReturn(true);
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(joinPoint.proceed()).thenReturn("result");
            when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("序列化失败"));

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                auditAspect.around(joinPoint);

                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("参数过长截断")
        void around_longParamsTruncated() throws Throwable {
            String longParam = "x".repeat(600);
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{longParam});
            when(joinPoint.proceed()).thenReturn("result");
            when(objectMapper.writeValueAsString(any())).thenReturn("\"" + longParam + "\"");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                auditAspect.around(joinPoint);

                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("结果过长截断")
        void around_longResultTruncated() throws Throwable {
            String longResult = "x".repeat(600);
            when(auditLogAnnotation.recordResult()).thenReturn(true);
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(joinPoint.proceed()).thenReturn(longResult);
            when(objectMapper.writeValueAsString(any())).thenReturn("\"" + longResult + "\"");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                auditAspect.around(joinPoint);

                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("返回结果为null时不记录")
        void around_nullResultNotRecorded() throws Throwable {
            when(auditLogAnnotation.recordResult()).thenReturn(true);
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(joinPoint.proceed()).thenReturn(null);

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                auditAspect.around(joinPoint);

                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("参数为null数组")
        void around_nullArgs() throws Throwable {
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(null);
            when(joinPoint.proceed()).thenReturn("result");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                auditAspect.around(joinPoint);

                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("过滤HttpServletRequest参数")
        void around_filterHttpServletRequestArg() throws Throwable {
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{mock(org.springframework.mock.web.MockHttpServletRequest.class)});
            when(joinPoint.proceed()).thenReturn("result");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                auditAspect.around(joinPoint);

                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("所有参数都被过滤后为空")
        void around_allArgsFiltered() throws Throwable {
            jakarta.servlet.http.HttpServletResponse mockResponse = mock(jakarta.servlet.http.HttpServletResponse.class);
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{mockResponse});
            when(joinPoint.proceed()).thenReturn("result");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(null);

                auditAspect.around(joinPoint);

                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }
    }

    @Nested
    @DisplayName("IP地址获取测试")
    class IpAddressTests {

        @Test
        @DisplayName("通过 X-Forwarded-For 获取IP")
        void getIpFromXForwardedFor() throws Throwable {
            org.springframework.mock.web.MockHttpServletRequest mockRequest = new org.springframework.mock.web.MockHttpServletRequest();
            mockRequest.addHeader("X-Forwarded-For", "192.168.1.1");
            org.springframework.web.context.request.ServletRequestAttributes attributes =
                    new org.springframework.web.context.request.ServletRequestAttributes(mockRequest);

            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(joinPoint.proceed()).thenReturn("result");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);

                auditAspect.around(joinPoint);

                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("X-Forwarded-For 为 unknown 时从 X-Real-IP 获取")
        void getIpFromXRealIp() throws Throwable {
            org.springframework.mock.web.MockHttpServletRequest mockRequest = new org.springframework.mock.web.MockHttpServletRequest();
            mockRequest.addHeader("X-Forwarded-For", "unknown");
            mockRequest.addHeader("X-Real-IP", "192.168.1.2");
            org.springframework.web.context.request.ServletRequestAttributes attributes =
                    new org.springframework.web.context.request.ServletRequestAttributes(mockRequest);

            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(joinPoint.proceed()).thenReturn("result");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);

                auditAspect.around(joinPoint);

                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("无IP头时从 RemoteAddr 获取")
        void getIpFromRemoteAddr() throws Throwable {
            org.springframework.mock.web.MockHttpServletRequest mockRequest = new org.springframework.mock.web.MockHttpServletRequest();
            mockRequest.setRemoteAddr("192.168.1.3");
            org.springframework.web.context.request.ServletRequestAttributes attributes =
                    new org.springframework.web.context.request.ServletRequestAttributes(mockRequest);

            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(joinPoint.proceed()).thenReturn("result");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);

                auditAspect.around(joinPoint);

                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("X-Forwarded-For 包含多个IP时取第一个")
        void getFirstIpFromMultiple() throws Throwable {
            org.springframework.mock.web.MockHttpServletRequest mockRequest = new org.springframework.mock.web.MockHttpServletRequest();
            mockRequest.addHeader("X-Forwarded-For", "192.168.1.4, 10.0.0.1, 172.16.0.1");
            org.springframework.web.context.request.ServletRequestAttributes attributes =
                    new org.springframework.web.context.request.ServletRequestAttributes(mockRequest);

            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(joinPoint.proceed()).thenReturn("result");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);

                auditAspect.around(joinPoint);

                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }

        @Test
        @DisplayName("X-Forwarded-For 为空字符串时从 X-Real-IP 获取")
        void getIpWhenXForwardedForEmpty() throws Throwable {
            org.springframework.mock.web.MockHttpServletRequest mockRequest = new org.springframework.mock.web.MockHttpServletRequest();
            mockRequest.addHeader("X-Forwarded-For", "");
            mockRequest.addHeader("X-Real-IP", "192.168.1.5");
            org.springframework.web.context.request.ServletRequestAttributes attributes =
                    new org.springframework.web.context.request.ServletRequestAttributes(mockRequest);

            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(method);
            when(method.getAnnotation(AuditLog.class)).thenReturn(auditLogAnnotation);
            when(signature.getDeclaringType()).thenReturn(AuditAspectTest.class);
            when(signature.getName()).thenReturn("mockMethod");
            when(joinPoint.getArgs()).thenReturn(new Object[]{});
            when(joinPoint.proceed()).thenReturn("result");

            try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class);
                 MockedStatic<RequestContextHolder> mockedRequestContext = mockStatic(RequestContextHolder.class)) {
                mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(1L);
                mockedUserContext.when(UserContext::getCurrentUsername).thenReturn("testuser");
                mockedRequestContext.when(RequestContextHolder::getRequestAttributes).thenReturn(attributes);

                auditAspect.around(joinPoint);

                verify(auditLogService).saveLogAsync(any(AuditLogEntity.class));
            }
        }
    }
}