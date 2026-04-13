package com.yhj.dictation.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审计日志实体
 */
@Data
@Entity
@Table(name = "audit_log")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "operation", nullable = false)
    private String operation;

    @Column(name = "method")
    private String method;

    @Column(name = "params", length = 2000)
    private String params;

    @Column(name = "result", length = 2000)
    private String result;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;
}