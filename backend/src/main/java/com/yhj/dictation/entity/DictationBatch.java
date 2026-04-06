package com.yhj.dictation.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 听写批次实体
 */
@Data
@Entity
@Table(name = "dictation_batch")
public class DictationBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_name", nullable = false)
    private String batchName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "total_words", nullable = false)
    private Integer totalWords = 0;

    @Column(name = "completed_words", nullable = false)
    private Integer completedWords = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BatchStatus status = BatchStatus.CREATED;

    public enum BatchStatus {
        CREATED,      // 已创建
        IN_PROGRESS,  // 进行中
        COMPLETED,    // 已完成
        CANCELLED     // 已取消
    }
}