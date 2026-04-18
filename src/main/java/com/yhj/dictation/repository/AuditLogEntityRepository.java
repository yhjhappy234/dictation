package com.yhj.dictation.repository;

import com.yhj.dictation.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志 Repository
 */
@Repository
public interface AuditLogEntityRepository extends JpaRepository<AuditLogEntity, Long>, JpaSpecificationExecutor<AuditLogEntity> {

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

    /**
     * 分页查询所有日志（按时间倒序）
     */
    Page<AuditLogEntity> findAllByOrderByTimestampDesc(Pageable pageable);

    /**
     * 根据用户名分页查询（按时间倒序）
     */
    Page<AuditLogEntity> findByUsernameOrderByTimestampDesc(String username, Pageable pageable);

    /**
     * 根据操作类型分页查询（按时间倒序）
     */
    Page<AuditLogEntity> findByOperationOrderByTimestampDesc(String operation, Pageable pageable);

    /**
     * 根据时间范围分页查询（按时间倒序）
     */
    Page<AuditLogEntity> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * 根据用户名和时间范围分页查询
     */
    Page<AuditLogEntity> findByUsernameAndTimestampBetweenOrderByTimestampDesc(String username, LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * 根据操作类型和时间范围分页查询
     */
    Page<AuditLogEntity> findByOperationAndTimestampBetweenOrderByTimestampDesc(String operation, LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * 统计指定时间范围内的日志数量
     */
    long countByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 统计指定用户的日志数量
     */
    long countByUsername(String username);

    /**
     * 统计指定操作的日志数量
     */
    long countByOperation(String operation);
}