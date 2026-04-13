package com.yhj.dictation.repository;

import com.yhj.dictation.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志 Repository
 */
@Repository
public interface AuditLogEntityRepository extends JpaRepository<AuditLogEntity, Long> {

    /**
     * 根据用户ID查找日志
     */
    List<AuditLogEntity> findByUserId(Long userId);

    /**
     * 根据用户名查找日志
     */
    List<AuditLogEntity> findByUsername(String username);

    /**
     * 根据操作类型查找日志
     */
    List<AuditLogEntity> findByOperation(String operation);

    /**
     * 根据时间范围查找日志
     */
    List<AuditLogEntity> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 根据用户ID和时间范围查找日志
     */
    List<AuditLogEntity> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);
}