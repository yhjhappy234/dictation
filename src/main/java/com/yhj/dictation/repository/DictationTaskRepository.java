package com.yhj.dictation.repository;

import com.yhj.dictation.entity.DictationTask;
import com.yhj.dictation.entity.DictationTask.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 听写任务Repository
 */
@Repository
public interface DictationTaskRepository extends JpaRepository<DictationTask, Long> {

    /**
     * 获取所有任务（按创建时间降序）
     */
    List<DictationTask> findAllByOrderByCreatedAtDesc();

    /**
     * 获取收藏的任务
     */
    List<DictationTask> findByIsFavoriteTrueOrderByCreatedAtDesc();

    /**
     * 获取指定状态的任务（按创建时间降序）
     */
    List<DictationTask> findByStatusOrderByCreatedAtDesc(TaskStatus status);

    /**
     * 获取未完成的任务（未开始+进行中）
     */
    List<DictationTask> findByStatusInOrderByCreatedAtDesc(List<TaskStatus> statuses);
}