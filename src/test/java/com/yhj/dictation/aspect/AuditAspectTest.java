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
    }
}