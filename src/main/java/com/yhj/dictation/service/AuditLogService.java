package com.yhj.dictation.service;

import com.yhj.dictation.entity.AuditLogEntity;
import com.yhj.dictation.repository.AuditLogEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 分页查询审计日志
     */
    public Page<AuditLogEntity> getAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }

    /**
     * 条件查询审计日志
     */
    public Page<AuditLogEntity> searchAuditLogs(String username, String operation,
                                                 LocalDateTime startTime, LocalDateTime endTime,
                                                 int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        Specification<AuditLogEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (username != null && !username.isEmpty()) {
                predicates = cb.and(predicates, cb.equal(root.get("username"), username));
            }

            if (operation != null && !operation.isEmpty()) {
                predicates = cb.and(predicates, cb.equal(root.get("operation"), operation));
            }

            if (startTime != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("timestamp"), startTime));
            }

            if (endTime != null) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("timestamp"), endTime));
            }

            return predicates;
        };

        return auditLogRepository.findAll(spec, pageable);
    }

    /**
     * 根据ID获取审计日志详情
     */
    public AuditLogEntity getAuditLogById(Long id) {
        return auditLogRepository.findById(id).orElse(null);
    }

    /**
     * 统计日志数量
     */
    public long countAuditLogs(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            return auditLogRepository.countByTimestampBetween(startTime, endTime);
        }
        return auditLogRepository.count();
    }

    /**
     * 获取所有用户名列表（用于筛选）
     */
    public List<String> getAllUsernames() {
        return auditLogRepository.findAll()
                .stream()
                .map(AuditLogEntity::getUsername)
                .distinct()
                .toList();
    }

    /**
     * 获取所有操作类型列表（用于筛选）
     */
    public List<String> getAllOperations() {
        return auditLogRepository.findAll()
                .stream()
                .map(AuditLogEntity::getOperation)
                .distinct()
                .toList();
    }
}