package com.yhj.dictation.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 听写任务实体
 * 用于保存可重复使用的听写任务
 */
@Data
@Entity
@Table(name = "dictation_task")
public class DictationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(name = "words", nullable = false)
    private String words;

    @Column(name = "word_count", nullable = false)
    private Integer wordCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_favorite")
    private Boolean isFavorite = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.NOT_STARTED;

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        NOT_STARTED,    // 未开始
        IN_PROGRESS,    // 进行中
        COMPLETED       // 已完成
    }
}