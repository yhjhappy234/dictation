package com.yhj.dictation.repository;

import com.yhj.dictation.entity.DictationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 听写任务Repository
 */
@Repository
public interface DictationTaskRepository extends JpaRepository<DictationTask, Long> {

    /**
     * 获取所有任务模板（按创建时间降序）
     */
    List<DictationTask> findAllByOrderByCreatedAtDesc();

    /**
     * 获取收藏的任务模板
     */
    List<DictationTask> findByIsFavoriteTrueOrderByCreatedAtDesc();
}