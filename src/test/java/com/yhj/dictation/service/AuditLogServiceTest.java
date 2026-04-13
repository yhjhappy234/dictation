package com.yhj.dictation.service;

import com.yhj.dictation.entity.AuditLogEntity;
import com.yhj.dictation.repository.AuditLogEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AuditLogService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogEntityRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private AuditLogEntity testAuditLog;

    @BeforeEach
    void setUp() {
        testAuditLog = new AuditLogEntity();
        testAuditLog.setId(1L);
        testAuditLog.setUserId(1L);
        testAuditLog.setUsername("testuser");
        testAuditLog.setOperation("用户登录");
        testAuditLog.setMethod("AuthController.login");
        testAuditLog.setTimestamp(LocalDateTime.now());
        testAuditLog.setSuccess(true);
    }

    @Nested
    @DisplayName("saveLogAsync 方法测试")
    class SaveLogAsyncTests {

        @Test
        @DisplayName("异步保存日志成功")
        void saveLogAsync_success() {
            when(auditLogRepository.save(any(AuditLogEntity.class))).thenReturn(testAuditLog);

            auditLogService.saveLogAsync(testAuditLog);

            verify(auditLogRepository, timeout(1000)).save(any(AuditLogEntity.class));
        }

        @Test
        @DisplayName("异步保存日志 - 异常情况")
        void saveLogAsync_exception() {
            when(auditLogRepository.save(any(AuditLogEntity.class))).thenThrow(new RuntimeException("Database error"));

            auditLogService.saveLogAsync(testAuditLog);

            // 异步调用，应该不抛出异常
            verify(auditLogRepository, timeout(1000)).save(any(AuditLogEntity.class));
        }

        @Test
        @DisplayName("异步保存日志 - 简化版")
        void saveLogAsync_simplified_success() {
            when(auditLogRepository.save(any(AuditLogEntity.class))).thenReturn(testAuditLog);

            auditLogService.saveLogAsync(1L, "testuser", "用户登录", "AuthController.login",
                    "params", "result", "127.0.0.1", 100L, true, null);

            verify(auditLogRepository, timeout(1000)).save(any(AuditLogEntity.class));
        }
    }
}