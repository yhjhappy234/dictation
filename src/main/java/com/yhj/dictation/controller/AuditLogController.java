package com.yhj.dictation.controller;

import com.yhj.dictation.dto.ApiResponse;
import com.yhj.dictation.entity.AuditLogEntity;
import com.yhj.dictation.service.AuditLogService;
import com.yhj.dictation.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志控制器（管理员功能）
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * 分页获取审计日志列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogEntity>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // 检查是否是管理员
        if (!UserContext.isAdmin()) {
            return ResponseEntity.ok(ApiResponse.error("需要管理员权限"));
        }

        try {
            Page<AuditLogEntity> logs = auditLogService.getAuditLogs(page, size);
            return ResponseEntity.ok(ApiResponse.success(logs));
        } catch (Exception e) {
            log.error("Failed to get audit logs", e);
            return ResponseEntity.ok(ApiResponse.error("获取审计日志失败"));
        }
    }

    /**
     * 条件搜索审计日志
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<AuditLogEntity>>> searchAuditLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // 检查是否是管理员
        if (!UserContext.isAdmin()) {
            return ResponseEntity.ok(ApiResponse.error("需要管理员权限"));
        }

        try {
            Page<AuditLogEntity> logs = auditLogService.searchAuditLogs(username, operation, startTime, endTime, page, size);
            return ResponseEntity.ok(ApiResponse.success(logs));
        } catch (Exception e) {
            log.error("Failed to search audit logs", e);
            return ResponseEntity.ok(ApiResponse.error("搜索审计日志失败"));
        }
    }

    /**
     * 获取审计日志详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogEntity>> getAuditLogById(@PathVariable Long id) {

        // 检查是否是管理员
        if (!UserContext.isAdmin()) {
            return ResponseEntity.ok(ApiResponse.error("需要管理员权限"));
        }

        try {
            AuditLogEntity log = auditLogService.getAuditLogById(id);
            if (log == null) {
                return ResponseEntity.ok(ApiResponse.error("审计日志不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(log));
        } catch (Exception e) {
            log.error("Failed to get audit log by id: {}", id, e);
            return ResponseEntity.ok(ApiResponse.error("获取审计日志详情失败"));
        }
    }

    /**
     * 获取所有用户名列表（用于筛选下拉框）
     */
    @GetMapping("/usernames")
    public ResponseEntity<ApiResponse<List<String>>> getAllUsernames() {

        // 检查是否是管理员
        if (!UserContext.isAdmin()) {
            return ResponseEntity.ok(ApiResponse.error("需要管理员权限"));
        }

        try {
            List<String> usernames = auditLogService.getAllUsernames();
            return ResponseEntity.ok(ApiResponse.success(usernames));
        } catch (Exception e) {
            log.error("Failed to get usernames", e);
            return ResponseEntity.ok(ApiResponse.error("获取用户名列表失败"));
        }
    }

    /**
     * 获取所有操作类型列表（用于筛选下拉框）
     */
    @GetMapping("/operations")
    public ResponseEntity<ApiResponse<List<String>>> getAllOperations() {

        // 检查是否是管理员
        if (!UserContext.isAdmin()) {
            return ResponseEntity.ok(ApiResponse.error("需要管理员权限"));
        }

        try {
            List<String> operations = auditLogService.getAllOperations();
            return ResponseEntity.ok(ApiResponse.success(operations));
        } catch (Exception e) {
            log.error("Failed to get operations", e);
            return ResponseEntity.ok(ApiResponse.error("获取操作类型列表失败"));
        }
    }

    /**
     * 统计审计日志数量
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 检查是否是管理员
        if (!UserContext.isAdmin()) {
            return ResponseEntity.ok(ApiResponse.error("需要管理员权限"));
        }

        try {
            long count = auditLogService.countAuditLogs(startTime, endTime);
            return ResponseEntity.ok(ApiResponse.success(count));
        } catch (Exception e) {
            log.error("Failed to count audit logs", e);
            return ResponseEntity.ok(ApiResponse.error("统计审计日志失败"));
        }
    }
}