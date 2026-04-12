package com.yhj.dictation.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务听写记录实体
 * 记录每个词语的听写结果
 */
@Data
@Entity
@Table(name = "task_record")
public class TaskRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "word", nullable = false)
    private String word;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 预构造方法
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (errorCount == null) {
            errorCount = 0;
        }
    }
}