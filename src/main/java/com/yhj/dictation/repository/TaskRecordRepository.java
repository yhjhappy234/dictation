package com.yhj.dictation.repository;

import com.yhj.dictation.entity.TaskRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 任务听写记录Repository
 */
@Repository
public interface TaskRecordRepository extends JpaRepository<TaskRecord, Long> {

    /**
     * 获取任务的所有听写记录
     */
    List<TaskRecord> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    /**
     * 获取任务的错误记录
     */
    List<TaskRecord> findByTaskIdAndIsCorrectFalse(Long taskId);

    /**
     * 获取任务的正确记录
     */
    List<TaskRecord> findByTaskIdAndIsCorrectTrue(Long taskId);

    /**
     * 统计任务的正确数
     */
    Long countByTaskIdAndIsCorrectTrue(Long taskId);

    /**
     * 统计任务的错误数
     */
    Long countByTaskIdAndIsCorrectFalse(Long taskId);

    /**
     * 删除任务的所有记录
     */
    void deleteByTaskId(Long taskId);

    /**
     * 获取任务的最新一条记录
     */
    Optional<TaskRecord> findFirstByTaskIdOrderByCreatedAtDesc(Long taskId);
}