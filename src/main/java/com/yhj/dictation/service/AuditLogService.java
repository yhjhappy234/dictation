package com.yhj.dictation.service;

import com.yhj.dictation.entity.AuditLogEntity;
import com.yhj.dictation.repository.AuditLogEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 审计日志服务
 * 所有日志保存操作都是异步的，不阻塞主流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogEntityRepository auditLogRepository;

    /**
     * 异步保存审计日志
     */
    @Async
    @Transactional
    public void saveLogAsync(AuditLogEntity auditLog) {
        try {
            auditLogRepository.save(auditLog);
            log.debug("审计日志已保存: {} - {}", auditLog.getUsername(), auditLog.getOperation());
        } catch (Exception e) {
            log.error("保存审计日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 异步保存审计日志（简化版）
     */
    @Async
    @Transactional
    public void saveLogAsync(Long userId, String username, String operation, String method,
                            String params, String result, String ipAddress, Long durationMs,
                            Boolean success, String errorMessage) {
        try {
            AuditLogEntity auditLog = new AuditLogEntity();
            auditLog.setUserId(userId);
            auditLog.setUsername(username);
            auditLog.setOperation(operation);
            auditLog.setMethod(method);
            auditLog.setParams(params);
            auditLog.setResult(result);
            auditLog.setIpAddress(ipAddress);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setDurationMs(durationMs);
            auditLog.setSuccess(success);
            auditLog.setErrorMessage(errorMessage);

            auditLogRepository.save(auditLog);
            log.debug("审计日志已保存: {} - {}", username, operation);
        } catch (Exception e) {
            log.error("保存审计日志失败: {}", e.getMessage(), e);
        }
    }
}